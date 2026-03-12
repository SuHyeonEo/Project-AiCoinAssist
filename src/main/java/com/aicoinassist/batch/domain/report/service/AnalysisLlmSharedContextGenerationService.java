package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmRetryPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextResolution;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportSharedContextEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportSharedContextRepository;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisLlmSharedContextGenerationService {

    private final AnalysisLlmNarrativeProperties analysisLlmNarrativeProperties;
    private final AnalysisLlmSharedContextInputAssembler analysisLlmSharedContextInputAssembler;
    private final AnalysisLlmSharedContextPromptComposer analysisLlmSharedContextPromptComposer;
    private final AnalysisLlmNarrativeGateway analysisLlmNarrativeGateway;
    private final AnalysisReportSharedContextRepository analysisReportSharedContextRepository;
    private final AnalysisReportSharedContextDraftFactory analysisReportSharedContextDraftFactory;
    private final AnalysisReportSharedContextPersistenceService analysisReportSharedContextPersistenceService;
    private final OpenAiProperties openAiProperties;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public AnalysisLlmSharedContextResolution getOrGenerate(AnalysisGptReportInputPayload reportInput) {
        AnalysisLlmSharedContextInputPayload input = analysisLlmSharedContextInputAssembler.assemble(reportInput);
        AnalysisLlmSharedContextResolution stored = findStored(input);
        if (stored != null) {
            return stored;
        }

        AnalysisLlmPromptComposition composition = analysisLlmSharedContextPromptComposer.compose(input);
        return generateAndStore(reportInput, input, composition);
    }

    private AnalysisLlmSharedContextResolution generateAndStore(
            AnalysisGptReportInputPayload reportInput,
            AnalysisLlmSharedContextInputPayload input,
            AnalysisLlmPromptComposition composition
    ) {
        Instant requestedAt = clock.instant();
        AnalysisLlmSharedContextGenerationResult generationResult = generate(input, composition);
        Instant completedAt = clock.instant();
        Instant storedAt = clock.instant();
        AnalysisReportSharedContextEntity entity = analysisReportSharedContextPersistenceService.save(
                analysisReportSharedContextDraftFactory.create(
                        reportInput,
                        input,
                        generationResult,
                        analysisLlmNarrativeProperties.provider(),
                        openAiProperties.model(),
                        sharedPromptTemplateVersion(),
                        sharedInputSchemaVersion(),
                        sharedOutputSchemaVersion(),
                        requestedAt,
                        completedAt,
                        storedAt
                )
        );
        return toResolution(entity);
    }

    private AnalysisLlmSharedContextGenerationResult generate(
            AnalysisLlmSharedContextInputPayload input,
            AnalysisLlmPromptComposition composition
    ) {
        AnalysisLlmRetryPolicy retryPolicy = new AnalysisLlmRetryPolicy(analysisLlmNarrativeProperties.maxTransportAttempts());
        AnalysisLlmNarrativeGatewayRequest request = AnalysisLlmNarrativeGatewayRequest.from(composition);

        for (int attempt = 1; attempt <= retryPolicy.maxTransportAttempts(); attempt++) {
            try {
                AnalysisLlmNarrativeGatewayResponse response = analysisLlmNarrativeGateway.generate(request);
                AnalysisLlmSharedContextReference output = parseOutput(input, response.rawOutputJson());
                boolean fallbackUsed = output.contextVersion().equals(input.sharedContextVersion())
                        && output.sharedSummary() != null
                        && isFallbackOutput(input, output);
                return new AnalysisLlmSharedContextGenerationResult(
                        composition,
                        response,
                        output,
                        attempt,
                        fallbackUsed,
                        fallbackUsed ? AnalysisLlmNarrativeFailureType.CONTENT : AnalysisLlmNarrativeFailureType.NONE,
                        List.of()
                );
            } catch (Exception exception) {
                if (attempt >= retryPolicy.maxTransportAttempts()) {
                    AnalysisLlmNarrativeFailureType failureType = exception instanceof AnalysisLlmNarrativeGatewayException gatewayException
                            ? gatewayException.getFailureType()
                            : AnalysisLlmNarrativeFailureType.UNKNOWN;
                    return fallbackResult(input, composition, attempt, failureType, exception.getMessage());
                }
            }
        }
        return fallbackResult(
                input,
                composition,
                retryPolicy.maxTransportAttempts(),
                AnalysisLlmNarrativeFailureType.UNKNOWN,
                "Unknown shared context generation failure."
        );
    }

    private AnalysisLlmSharedContextReference parseOutput(
            AnalysisLlmSharedContextInputPayload input,
            String rawOutputJson
    ) {
        try {
            JsonNode root = objectMapper.readTree(rawOutputJson);
            String sharedSummary = text(root.path("shared_summary"));
            AnalysisLlmSharedContextDomainReference macro = domain(root.path("macro"));
            AnalysisLlmSharedContextDomainReference sentiment = domain(root.path("sentiment"));
            if (sharedSummary == null || macro == null || sentiment == null) {
                return fallback(input);
            }
            return new AnalysisLlmSharedContextReference(
                    input.sharedContextVersion(),
                    sharedSummary,
                    macro,
                    sentiment
            );
        } catch (Exception exception) {
            return fallback(input);
        }
    }

    private AnalysisLlmSharedContextDomainReference domain(JsonNode node) {
        String status = text(node.path("status"));
        String summary = text(node.path("summary"));
        String watchPoint = text(node.path("watch_point"));
        if (status == null || summary == null || watchPoint == null) {
            return null;
        }
        return new AnalysisLlmSharedContextDomainReference(status, summary, watchPoint);
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value.strip();
    }

    private AnalysisLlmSharedContextReference fallback(AnalysisLlmSharedContextInputPayload input) {
        return new AnalysisLlmSharedContextReference(
                input.sharedContextVersion(),
                firstNonBlank(
                        first(input.macroFacts()),
                        first(input.sentimentFacts()),
                        "공통 거시 및 심리 맥락은 구조화된 입력 범위에서 보수적으로 해석합니다."
                ),
                new AnalysisLlmSharedContextDomainReference(
                        inferStatus(input.macroFacts()),
                        firstNonBlank(first(input.macroFacts()), "공통 거시 맥락은 추가 확인이 필요합니다."),
                        "달러, 금리, 환율 조합의 방향을 추가로 확인할 필요가 있습니다."
                ),
                new AnalysisLlmSharedContextDomainReference(
                        inferStatus(input.sentimentFacts()),
                        firstNonBlank(first(input.sentimentFacts()), "공통 심리 맥락은 추가 확인이 필요합니다."),
                        "Fear & Greed 분류와 평균 대비 편차를 추가로 확인할 필요가 있습니다."
                )
        );
    }

    private AnalysisLlmSharedContextGenerationResult fallbackResult(
            AnalysisLlmSharedContextInputPayload input,
            AnalysisLlmPromptComposition composition,
            int attempts,
            AnalysisLlmNarrativeFailureType failureType,
            String issue
    ) {
        return new AnalysisLlmSharedContextGenerationResult(
                composition,
                null,
                fallback(input),
                attempts,
                true,
                failureType,
                issue == null || issue.isBlank() ? List.of() : List.of(issue)
        );
    }

    private String inferStatus(java.util.List<String> facts) {
        String joined = facts == null ? "" : String.join(" ", facts).toLowerCase();
        if (joined.contains("fear") || joined.contains("부담") || joined.contains("headwind") || joined.contains("risk")) {
            return "BEARISH";
        }
        if (joined.contains("constructive") || joined.contains("우호") || joined.contains("supportive")) {
            return "BULLISH";
        }
        return "MIXED";
    }

    private String first(java.util.List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String sha256(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }

    private AnalysisLlmSharedContextResolution findStored(AnalysisLlmSharedContextInputPayload input) {
        return analysisReportSharedContextRepository
                .findTopByContextVersionAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        input.sharedContextVersion(),
                        analysisLlmNarrativeProperties.provider(),
                        openAiProperties.model(),
                        sharedPromptTemplateVersion(),
                        sharedInputSchemaVersion(),
                        sharedOutputSchemaVersion(),
                        sha256(analysisLlmSharedContextPromptComposer.compose(input).inputPayloadJson())
                )
                .map(this::toResolution)
                .orElse(null);
    }

    private AnalysisLlmSharedContextResolution toResolution(AnalysisReportSharedContextEntity entity) {
        try {
            return new AnalysisLlmSharedContextResolution(
                    entity.getId(),
                    entity.getContextVersion(),
                    objectMapper.readValue(entity.getOutputJson(), AnalysisLlmSharedContextReference.class)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize stored shared context output.", exception);
        }
    }

    private boolean isFallbackOutput(
            AnalysisLlmSharedContextInputPayload input,
            AnalysisLlmSharedContextReference output
    ) {
        AnalysisLlmSharedContextReference fallback = fallback(input);
        return fallback.equals(output);
    }

    private String sharedPromptTemplateVersion() {
        return analysisLlmNarrativeProperties.promptTemplateVersion() + "-shared-context";
    }

    private String sharedInputSchemaVersion() {
        return analysisLlmNarrativeProperties.inputSchemaVersion() + "-shared-context";
    }

    private String sharedOutputSchemaVersion() {
        return analysisLlmNarrativeProperties.outputSchemaVersion() + "-shared-context";
    }
}

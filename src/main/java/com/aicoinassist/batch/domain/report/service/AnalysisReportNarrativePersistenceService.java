package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AnalysisReportNarrativePersistenceService {

    private final AnalysisReportNarrativeRepository analysisReportNarrativeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisReportNarrativeEntity save(AnalysisReportNarrativeDraft draft) {
        String inputPayloadHash = sha256(draft.inputPayloadJson());
        String serializedOutput = serialize(draft.outputPayload());

        AnalysisReportNarrativeEntity existingEntity = analysisReportNarrativeRepository
                .findTopByAnalysisReportIdAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        draft.analysisReport().getId(),
                        draft.llmProvider(),
                        draft.llmModel(),
                        draft.promptTemplateVersion(),
                        draft.inputSchemaVersion(),
                        draft.outputSchemaVersion(),
                        inputPayloadHash
                )
                .orElse(null);

        if (existingEntity == null) {
            AnalysisReportNarrativeEntity entity = AnalysisReportNarrativeEntity.builder()
                    .analysisReport(draft.analysisReport())
                    .symbol(draft.analysisReport().getSymbol())
                    .reportType(draft.analysisReport().getReportType())
                    .analysisBasisTime(draft.analysisReport().getAnalysisBasisTime())
                    .sourceDataVersion(draft.analysisReport().getSourceDataVersion())
                    .analysisEngineVersion(draft.analysisReport().getAnalysisEngineVersion())
                    .llmProvider(draft.llmProvider())
                    .llmModel(draft.llmModel())
                    .promptTemplateVersion(draft.promptTemplateVersion())
                    .inputSchemaVersion(draft.inputSchemaVersion())
                    .outputSchemaVersion(draft.outputSchemaVersion())
                    .inputPayloadHash(inputPayloadHash)
                    .inputPayloadJson(draft.inputPayloadJson())
                    .promptSystemText(draft.promptSystemText())
                    .promptUserText(draft.promptUserText())
                    .outputLengthPolicyJson(draft.outputLengthPolicyJson())
                    .referenceNewsJson(draft.referenceNewsJson())
                    .rawOutputText(draft.rawOutputText())
                    .outputJson(serializedOutput)
                    .fallbackUsed(draft.fallbackUsed())
                    .generationStatus(draft.generationStatus())
                    .failureType(draft.failureType())
                    .validationIssuesJson(draft.validationIssuesJson())
                    .providerRequestId(draft.providerRequestId())
                    .inputTokens(draft.inputTokens())
                    .outputTokens(draft.outputTokens())
                    .totalTokens(draft.totalTokens())
                    .requestedAt(draft.requestedAt())
                    .completedAt(draft.completedAt())
                    .storedAt(draft.storedAt())
                    .build();
            return analysisReportNarrativeRepository.save(entity);
        }

        existingEntity.refresh(
                draft.inputPayloadJson(),
                draft.promptSystemText(),
                draft.promptUserText(),
                draft.outputLengthPolicyJson(),
                draft.referenceNewsJson(),
                draft.rawOutputText(),
                serializedOutput,
                draft.fallbackUsed(),
                draft.generationStatus(),
                draft.failureType(),
                draft.validationIssuesJson(),
                draft.providerRequestId(),
                draft.inputTokens(),
                draft.outputTokens(),
                draft.totalTokens(),
                draft.requestedAt(),
                draft.completedAt(),
                draft.storedAt()
        );

        return existingEntity;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize analysis report narrative output.", exception);
        }
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
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisReportNarrativeDraftFactory {

    private final ObjectMapper objectMapper;

    public AnalysisReportNarrativeDraft create(
            AnalysisReportEntity analysisReport,
            AnalysisLlmNarrativeGenerationResult generationResult,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            List<AnalysisLlmReferenceNewsItem> referenceNews,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        return new AnalysisReportNarrativeDraft(
                analysisReport,
                llmProvider,
                llmModel,
                promptTemplateVersion,
                inputSchemaVersion,
                outputSchemaVersion,
                generationResult.promptComposition().inputPayloadJson(),
                generationResult.promptComposition().systemPrompt(),
                generationResult.promptComposition().userPrompt(),
                generationResult.promptComposition().outputLengthPolicyJson(),
                serialize(referenceNews == null ? List.of() : referenceNews),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().rawOutputJson(),
                generationResult.outputProcessingResult().output(),
                generationResult.outputProcessingResult().fallbackUsed(),
                generationStatus(generationResult),
                generationResult.failureType(),
                serialize(generationResult.outputProcessingResult().issues()),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().providerRequestId(),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().inputTokens(),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().outputTokens(),
                totalTokens(generationResult),
                requestedAt,
                completedAt,
                storedAt
        );
    }

    private AnalysisLlmNarrativeGenerationStatus generationStatus(AnalysisLlmNarrativeGenerationResult generationResult) {
        if (generationResult.outputProcessingResult() == null || generationResult.outputProcessingResult().output() == null) {
            return AnalysisLlmNarrativeGenerationStatus.FAILED;
        }
        return generationResult.degraded()
                ? AnalysisLlmNarrativeGenerationStatus.FALLBACK
                : AnalysisLlmNarrativeGenerationStatus.SUCCESS;
    }

    private Integer totalTokens(AnalysisLlmNarrativeGenerationResult generationResult) {
        if (generationResult.gatewayResponse() == null
                || generationResult.gatewayResponse().inputTokens() == null
                || generationResult.gatewayResponse().outputTokens() == null) {
            return null;
        }
        return generationResult.gatewayResponse().inputTokens() + generationResult.gatewayResponse().outputTokens();
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize analysis report narrative payload.", exception);
        }
    }
}

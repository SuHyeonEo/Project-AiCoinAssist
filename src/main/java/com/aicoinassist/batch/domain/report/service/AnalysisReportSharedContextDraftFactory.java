package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSharedContextDraft;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisReportSharedContextDraftFactory {

    private final ObjectMapper objectMapper;

    public AnalysisReportSharedContextDraft create(
            AnalysisLlmSharedContextInputPayload input,
            AnalysisLlmSharedContextGenerationResult generationResult,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        return new AnalysisReportSharedContextDraft(
                input.reportType(),
                input.analysisBasisTime(),
                input.rawReferenceTime(),
                input.sharedContextVersion(),
                input.analysisEngineVersion(),
                llmProvider,
                llmModel,
                promptTemplateVersion,
                inputSchemaVersion,
                outputSchemaVersion,
                generationResult.promptComposition().inputPayloadJson(),
                generationResult.promptComposition().systemPrompt(),
                generationResult.promptComposition().userPrompt(),
                generationResult.promptComposition().outputLengthPolicyJson(),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().rawOutputJson(),
                generationResult.output(),
                generationResult.fallbackUsed(),
                generationStatus(generationResult),
                generationResult.failureType(),
                serialize(mergedIssues(generationResult)),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().providerRequestId(),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().inputTokens(),
                generationResult.gatewayResponse() == null ? null : generationResult.gatewayResponse().outputTokens(),
                totalTokens(generationResult),
                requestedAt,
                completedAt,
                storedAt
        );
    }

    private List<String> mergedIssues(AnalysisLlmSharedContextGenerationResult generationResult) {
        List<String> merged = new ArrayList<>();
        if (generationResult.issues() != null) {
            generationResult.issues().stream()
                    .filter(issue -> issue != null && !issue.isBlank())
                    .forEach(merged::add);
        }
        return merged;
    }

    private AnalysisLlmNarrativeGenerationStatus generationStatus(AnalysisLlmSharedContextGenerationResult generationResult) {
        if (generationResult.output() == null) {
            return AnalysisLlmNarrativeGenerationStatus.FAILED;
        }
        return generationResult.fallbackUsed()
                ? AnalysisLlmNarrativeGenerationStatus.FALLBACK
                : AnalysisLlmNarrativeGenerationStatus.SUCCESS;
    }

    private Integer totalTokens(AnalysisLlmSharedContextGenerationResult generationResult) {
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
            throw new IllegalStateException("Failed to serialize analysis report shared context payload.", exception);
        }
    }
}

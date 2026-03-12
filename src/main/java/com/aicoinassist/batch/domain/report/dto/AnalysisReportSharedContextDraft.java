package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;

import java.time.Instant;

public record AnalysisReportSharedContextDraft(
        Instant analysisBasisTime,
        Instant rawReferenceTime,
        String contextVersion,
        String analysisEngineVersion,
        String llmProvider,
        String llmModel,
        String promptTemplateVersion,
        String inputSchemaVersion,
        String outputSchemaVersion,
        String inputPayloadJson,
        String promptSystemText,
        String promptUserText,
        String outputLengthPolicyJson,
        String rawOutputText,
        AnalysisLlmSharedContextReference outputPayload,
        boolean fallbackUsed,
        AnalysisLlmNarrativeGenerationStatus generationStatus,
        AnalysisLlmNarrativeFailureType failureType,
        String validationIssuesJson,
        String providerRequestId,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        Instant requestedAt,
        Instant completedAt,
        Instant storedAt
) {
}

package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record AnalysisReportNarrativeView(
        Long id,
        Long analysisReportId,
        String symbol,
        AnalysisReportType reportType,
        Instant analysisBasisTime,
        String sourceDataVersion,
        String analysisEngineVersion,
        String llmProvider,
        String llmModel,
        String promptTemplateVersion,
        String inputSchemaVersion,
        String outputSchemaVersion,
        boolean fallbackUsed,
        AnalysisLlmNarrativeGenerationStatus generationStatus,
        AnalysisLlmNarrativeFailureType failureType,
        String providerRequestId,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        Instant requestedAt,
        Instant completedAt,
        Instant storedAt,
        JsonNode inputPayload,
        String promptSystemText,
        String promptUserText,
        JsonNode outputLengthPolicy,
        JsonNode referenceNews,
        String rawOutputText,
        JsonNode output,
        JsonNode validationIssues
) {
}

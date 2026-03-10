package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;

import java.util.List;

public record AnalysisLlmNarrativeGenerationResult(
        AnalysisLlmPromptComposition promptComposition,
        AnalysisLlmNarrativeGatewayResponse gatewayResponse,
        AnalysisLlmOutputProcessingResult outputProcessingResult,
        int attempts,
        boolean degraded,
        AnalysisLlmNarrativeFailureType failureType,
        List<String> transportIssues
) {
}

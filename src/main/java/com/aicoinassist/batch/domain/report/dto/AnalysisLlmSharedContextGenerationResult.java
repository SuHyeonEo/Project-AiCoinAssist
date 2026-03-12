package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;

import java.util.List;

public record AnalysisLlmSharedContextGenerationResult(
        AnalysisLlmPromptComposition promptComposition,
        AnalysisLlmNarrativeGatewayResponse gatewayResponse,
        AnalysisLlmSharedContextReference output,
        int attempts,
        boolean fallbackUsed,
        AnalysisLlmNarrativeFailureType failureType,
        List<String> issues
) {
}

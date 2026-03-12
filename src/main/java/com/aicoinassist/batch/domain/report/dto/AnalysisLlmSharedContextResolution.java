package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmSharedContextResolution(
        Long sharedContextId,
        String contextVersion,
        AnalysisLlmSharedContextReference reference
) {
}

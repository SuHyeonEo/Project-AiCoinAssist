package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmNarrativeGatewayResponse(
        String rawOutputJson,
        String providerModel,
        String providerRequestId,
        Integer inputTokens,
        Integer outputTokens
) {
}

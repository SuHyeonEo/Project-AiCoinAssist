package com.aicoinassist.batch.domain.news.dto;

public record ReferenceNewsGatewayResponse(
        String rawOutputJson,
        String model,
        String providerRequestId,
        Integer inputTokens,
        Integer outputTokens
) {
}

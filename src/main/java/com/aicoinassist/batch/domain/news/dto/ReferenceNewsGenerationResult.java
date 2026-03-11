package com.aicoinassist.batch.domain.news.dto;

public record ReferenceNewsGenerationResult(
        ReferenceNewsPromptComposition promptComposition,
        ReferenceNewsGatewayResponse gatewayResponse,
        ReferenceNewsSnapshotPayload payload,
        int attempts
) {
}

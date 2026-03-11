package com.aicoinassist.batch.domain.news.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ReferenceNewsSnapshot(
        Long id,
        String scope,
        LocalDate snapshotDate,
        String llmProvider,
        String llmModel,
        String promptTemplateVersion,
        String inputSchemaVersion,
        String outputSchemaVersion,
        int articleCount,
        String inputPayloadJson,
        String promptSystemText,
        String promptUserText,
        String outputLengthPolicyJson,
        String rawOutputText,
        ReferenceNewsSnapshotPayload payload,
        String providerRequestId,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        Instant requestedAt,
        Instant completedAt,
        Instant storedAt
) {
}

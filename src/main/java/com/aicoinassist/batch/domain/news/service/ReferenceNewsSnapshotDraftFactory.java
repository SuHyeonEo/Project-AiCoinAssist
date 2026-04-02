package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGenerationResult;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReferenceNewsSnapshotDraftFactory {

    private final ReferenceNewsProperties referenceNewsProperties;

    public ReferenceNewsSnapshotDraft create(
            LocalDate snapshotDate,
            ReferenceNewsGenerationResult generationResult,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        return new ReferenceNewsSnapshotDraft(
                referenceNewsProperties.scope(),
                snapshotDate,
                referenceNewsProperties.provider(),
                generationResult.gatewayResponse().model(),
                referenceNewsProperties.promptTemplateVersion(),
                referenceNewsProperties.inputSchemaVersion(),
                referenceNewsProperties.outputSchemaVersion(),
                generationResult.promptComposition().inputPayloadJson(),
                generationResult.promptComposition().systemPrompt(),
                generationResult.promptComposition().userPrompt(),
                generationResult.promptComposition().outputLengthPolicyJson(),
                generationResult.gatewayResponse().rawOutputJson(),
                generationResult.payload(),
                generationResult.gatewayResponse().providerRequestId(),
                generationResult.gatewayResponse().inputTokens(),
                generationResult.gatewayResponse().outputTokens(),
                totalTokens(generationResult),
                requestedAt,
                completedAt,
                storedAt
        );
    }

    private Integer totalTokens(ReferenceNewsGenerationResult generationResult) {
        if (generationResult.gatewayResponse().inputTokens() == null
                || generationResult.gatewayResponse().outputTokens() == null) {
            return null;
        }
        return generationResult.gatewayResponse().inputTokens() + generationResult.gatewayResponse().outputTokens();
    }
}

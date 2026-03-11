package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshot;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.ReferenceNewsSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceNewsSnapshotReadService {

    private final ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;
    private final ReferenceNewsProperties referenceNewsProperties;
    private final ObjectMapper objectMapper;

    public ReferenceNewsSnapshot getLatest() {
        return getLatest(referenceNewsProperties.scope());
    }

    public ReferenceNewsSnapshot getLatest(String scope) {
        return referenceNewsSnapshotRepository.findTopByScopeOrderBySnapshotDateDescIdDesc(scope)
                .map(this::toSnapshot)
                .orElseThrow(() -> new IllegalArgumentException("Reference news snapshot not found: " + scope));
    }

    private ReferenceNewsSnapshot toSnapshot(ReferenceNewsSnapshotEntity entity) {
        return new ReferenceNewsSnapshot(
                entity.getId(),
                entity.getScope(),
                entity.getSnapshotDate(),
                entity.getLlmProvider(),
                entity.getLlmModel(),
                entity.getPromptTemplateVersion(),
                entity.getInputSchemaVersion(),
                entity.getOutputSchemaVersion(),
                entity.getArticleCount(),
                entity.getInputPayloadJson(),
                entity.getPromptSystemText(),
                entity.getPromptUserText(),
                entity.getOutputLengthPolicyJson(),
                entity.getRawOutputText(),
                readPayload(entity.getPayloadJson()),
                entity.getProviderRequestId(),
                entity.getInputTokens(),
                entity.getOutputTokens(),
                entity.getTotalTokens(),
                entity.getRequestedAt(),
                entity.getCompletedAt(),
                entity.getStoredAt()
        );
    }

    private ReferenceNewsSnapshotPayload readPayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, ReferenceNewsSnapshotPayload.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize reference news snapshot payload.", exception);
        }
    }
}

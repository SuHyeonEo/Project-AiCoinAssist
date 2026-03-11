package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotDraft;
import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.ReferenceNewsSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferenceNewsSnapshotPersistenceService {

    private final ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReferenceNewsSnapshotEntity save(ReferenceNewsSnapshotDraft draft) {
        String payloadJson = serialize(draft.payload());
        int articleCount = draft.payload() == null || draft.payload().items() == null
                ? 0
                : draft.payload().items().size();

        ReferenceNewsSnapshotEntity existingEntity = referenceNewsSnapshotRepository
                .findTopByScopeAndSnapshotDateOrderByIdDesc(draft.scope(), draft.snapshotDate())
                .orElse(null);

        if (existingEntity == null) {
            return referenceNewsSnapshotRepository.save(
                    ReferenceNewsSnapshotEntity.builder()
                            .scope(draft.scope())
                            .snapshotDate(draft.snapshotDate())
                            .llmProvider(draft.llmProvider())
                            .llmModel(draft.llmModel())
                            .promptTemplateVersion(draft.promptTemplateVersion())
                            .inputSchemaVersion(draft.inputSchemaVersion())
                            .outputSchemaVersion(draft.outputSchemaVersion())
                            .articleCount(articleCount)
                            .inputPayloadJson(draft.inputPayloadJson())
                            .promptSystemText(draft.promptSystemText())
                            .promptUserText(draft.promptUserText())
                            .outputLengthPolicyJson(draft.outputLengthPolicyJson())
                            .rawOutputText(draft.rawOutputText())
                            .payloadJson(payloadJson)
                            .providerRequestId(draft.providerRequestId())
                            .inputTokens(draft.inputTokens())
                            .outputTokens(draft.outputTokens())
                            .totalTokens(draft.totalTokens())
                            .requestedAt(draft.requestedAt())
                            .completedAt(draft.completedAt())
                            .storedAt(draft.storedAt())
                            .build()
            );
        }

        existingEntity.refresh(
                draft.llmProvider(),
                draft.llmModel(),
                draft.promptTemplateVersion(),
                draft.inputSchemaVersion(),
                draft.outputSchemaVersion(),
                articleCount,
                draft.inputPayloadJson(),
                draft.promptSystemText(),
                draft.promptUserText(),
                draft.outputLengthPolicyJson(),
                draft.rawOutputText(),
                payloadJson,
                draft.providerRequestId(),
                draft.inputTokens(),
                draft.outputTokens(),
                draft.totalTokens(),
                draft.requestedAt(),
                draft.completedAt(),
                draft.storedAt()
        );
        return existingEntity;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize reference news snapshot payload.", exception);
        }
    }
}

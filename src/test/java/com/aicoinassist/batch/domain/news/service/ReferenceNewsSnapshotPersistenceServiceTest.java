package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsCategory;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsItem;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotDraft;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.ReferenceNewsSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceNewsSnapshotPersistenceServiceTest {

    @Mock
    private ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void saveRefreshesExistingSnapshotWhenDateAndScopeMatch() {
        ReferenceNewsSnapshotPersistenceService service =
                new ReferenceNewsSnapshotPersistenceService(referenceNewsSnapshotRepository, objectMapper);
        ReferenceNewsSnapshotDraft draft = draft("Updated selection");
        ReferenceNewsSnapshotEntity entity = ReferenceNewsSnapshotEntity.builder()
                .scope("GLOBAL_CRYPTO")
                .snapshotDate(LocalDate.parse("2026-03-12"))
                .llmProvider("openai")
                .llmModel("gpt-5-mini")
                .promptTemplateVersion("reference-news-prompt-v1")
                .inputSchemaVersion("reference-news-input-v1")
                .outputSchemaVersion("reference-news-output-v1")
                .articleCount(1)
                .inputPayloadJson("{\"scope\":\"GLOBAL_CRYPTO\"}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{\"max_items\":5}")
                .payloadJson("{\"summary\":\"old\",\"items\":[]}")
                .providerRequestId("req_old")
                .inputTokens(100)
                .outputTokens(200)
                .totalTokens(300)
                .requestedAt(Instant.parse("2026-03-12T00:00:00Z"))
                .completedAt(Instant.parse("2026-03-12T00:00:01Z"))
                .storedAt(Instant.parse("2026-03-12T00:00:02Z"))
                .build();

        when(referenceNewsSnapshotRepository.findTopByScopeAndSnapshotDateOrderByIdDesc("GLOBAL_CRYPTO", LocalDate.parse("2026-03-12")))
                .thenReturn(Optional.of(entity));

        ReferenceNewsSnapshotEntity result = service.save(draft);

        verify(referenceNewsSnapshotRepository, never()).save(any(ReferenceNewsSnapshotEntity.class));
        assertThat(result).isSameAs(entity);
        assertThat(entity.getPayloadJson()).contains("Updated selection");
        assertThat(entity.getArticleCount()).isEqualTo(2);
    }

    @Test
    void savePersistsNewSnapshotWhenMissing() {
        ReferenceNewsSnapshotPersistenceService service =
                new ReferenceNewsSnapshotPersistenceService(referenceNewsSnapshotRepository, objectMapper);
        ReferenceNewsSnapshotDraft draft = draft("Fresh selection");

        when(referenceNewsSnapshotRepository.findTopByScopeAndSnapshotDateOrderByIdDesc("GLOBAL_CRYPTO", LocalDate.parse("2026-03-12")))
                .thenReturn(Optional.empty());
        when(referenceNewsSnapshotRepository.save(any(ReferenceNewsSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReferenceNewsSnapshotEntity result = service.save(draft);

        assertThat(result.getScope()).isEqualTo("GLOBAL_CRYPTO");
        assertThat(result.getLlmModel()).isEqualTo("gpt-5-mini");
        assertThat(result.getArticleCount()).isEqualTo(2);
        assertThat(result.getInputSchemaVersion()).isEqualTo("reference-news-input-v1");
        assertThat(result.getTotalTokens()).isEqualTo(500);
        assertThat(result.getPayloadJson()).contains("Fresh selection");
    }

    private ReferenceNewsSnapshotDraft draft(String summary) {
        return new ReferenceNewsSnapshotDraft(
                "GLOBAL_CRYPTO",
                LocalDate.parse("2026-03-12"),
                "openai",
                "gpt-5-mini",
                "reference-news-prompt-v1",
                "reference-news-input-v1",
                "reference-news-output-v1",
                "{\"scope\":\"GLOBAL_CRYPTO\",\"snapshot_date\":\"2026-03-12\"}",
                "system",
                "user",
                "{\"max_items\":5}",
                "{\"raw\":true}",
                new ReferenceNewsSnapshotPayload(
                        summary,
                        List.of(
                                new ReferenceNewsItem(
                                        ReferenceNewsCategory.DIRECT_ASSET,
                                        "Bitcoin ETF flow headline",
                                        "ExampleSource",
                                        Instant.parse("2026-03-12T00:30:00Z"),
                                        "https://example.com/news/1",
                                        "BTC 직접 관련도가 높습니다."
                                ),
                                new ReferenceNewsItem(
                                        ReferenceNewsCategory.MACRO_ECONOMY,
                                        "US CPI watch",
                                        "MacroSource",
                                        Instant.parse("2026-03-12T01:00:00Z"),
                                        "https://example.com/news/2",
                                        "거시 변수로 위험 선호에 영향을 줄 수 있습니다."
                                )
                        )
                ),
                "req_new",
                180,
                320,
                500,
                Instant.parse("2026-03-12T02:00:00Z"),
                Instant.parse("2026-03-12T02:00:05Z"),
                Instant.parse("2026-03-12T02:00:10Z")
        );
    }
}

package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshot;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceNewsSnapshotReadServiceTest {

    @Mock
    private ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void getLatestUsesConfiguredScope() {
        ReferenceNewsSnapshotReadService service = new ReferenceNewsSnapshotReadService(
                referenceNewsSnapshotRepository,
                new ReferenceNewsProperties(false, "openai", "GLOBAL_CRYPTO", "reference-news-prompt-v1", "reference-news-input-v1", "reference-news-output-v1", 1),
                objectMapper
        );

        when(referenceNewsSnapshotRepository.findTopByScopeOrderBySnapshotDateDescIdDesc("GLOBAL_CRYPTO"))
                .thenReturn(Optional.of(entity()));

        ReferenceNewsSnapshot snapshot = service.getLatest();

        assertThat(snapshot.scope()).isEqualTo("GLOBAL_CRYPTO");
        assertThat(snapshot.articleCount()).isEqualTo(2);
        assertThat(snapshot.payload().items()).hasSize(2);
        assertThat(snapshot.inputSchemaVersion()).isEqualTo("reference-news-input-v1");
        assertThat(snapshot.totalTokens()).isEqualTo(500);
    }

    @Test
    void getLatestThrowsWhenSnapshotMissing() {
        ReferenceNewsSnapshotReadService service = new ReferenceNewsSnapshotReadService(
                referenceNewsSnapshotRepository,
                new ReferenceNewsProperties(false, "openai", "GLOBAL_CRYPTO", "reference-news-prompt-v1", "reference-news-input-v1", "reference-news-output-v1", 1),
                objectMapper
        );

        when(referenceNewsSnapshotRepository.findTopByScopeOrderBySnapshotDateDescIdDesc("GLOBAL_CRYPTO"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(service::getLatest)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reference news snapshot not found: GLOBAL_CRYPTO");
    }

    private ReferenceNewsSnapshotEntity entity() {
        return ReferenceNewsSnapshotEntity.builder()
                .scope("GLOBAL_CRYPTO")
                .snapshotDate(LocalDate.parse("2026-03-12"))
                .llmProvider("openai")
                .llmModel("gpt-5-mini")
                .promptTemplateVersion("reference-news-prompt-v1")
                .inputSchemaVersion("reference-news-input-v1")
                .outputSchemaVersion("reference-news-output-v1")
                .articleCount(2)
                .inputPayloadJson("{\"scope\":\"GLOBAL_CRYPTO\",\"snapshot_date\":\"2026-03-12\"}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{\"max_items\":5}")
                .payloadJson("""
                        {
                          "summary":"Daily shared reference news",
                          "items":[
                            {
                              "category":"DIRECT_ASSET",
                              "title":"Bitcoin ETF flow headline",
                              "source":"ExampleSource",
                              "published_at":"2026-03-12T00:30:00Z",
                              "url":"https://example.com/news/1",
                              "selection_reason":"BTC 직접 관련도가 높습니다."
                            },
                            {
                              "category":"MACRO_ECONOMY",
                              "title":"US CPI watch",
                              "source":"MacroSource",
                              "published_at":"2026-03-12T01:00:00Z",
                              "url":"https://example.com/news/2",
                              "selection_reason":"거시 변수로 위험 선호에 영향을 줄 수 있습니다."
                            }
                          ]
                        }
                        """)
                .providerRequestId("req_ref_news_1")
                .inputTokens(150)
                .outputTokens(350)
                .totalTokens(500)
                .requestedAt(Instant.parse("2026-03-12T02:00:00Z"))
                .completedAt(Instant.parse("2026-03-12T02:00:05Z"))
                .storedAt(Instant.parse("2026-03-12T02:00:10Z"))
                .build();
    }
}

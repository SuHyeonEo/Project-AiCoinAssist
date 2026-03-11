package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGenerationResult;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsPromptComposition;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotDraft;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.ReferenceNewsSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceNewsSnapshotGenerationFlowServiceTest {

    @Mock
    private ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;

    @Mock
    private ReferenceNewsGenerationService referenceNewsGenerationService;

    @Mock
    private ReferenceNewsSnapshotDraftFactory referenceNewsSnapshotDraftFactory;

    @Mock
    private ReferenceNewsSnapshotPersistenceService referenceNewsSnapshotPersistenceService;

    @Test
    void generateForDateIfMissingSkipsWhenSnapshotAlreadyExists() {
        ReferenceNewsSnapshotEntity existing = ReferenceNewsSnapshotEntity.builder()
                .scope("GLOBAL_CRYPTO")
                .snapshotDate(LocalDate.parse("2026-03-12"))
                .llmProvider("openai")
                .llmModel("gpt-5-mini")
                .promptTemplateVersion("reference-news-prompt-v1")
                .inputSchemaVersion("reference-news-input-v1")
                .outputSchemaVersion("reference-news-output-v1")
                .articleCount(5)
                .inputPayloadJson("{}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{}")
                .payloadJson("{\"summary\":\"ok\",\"items\":[]}")
                .requestedAt(Instant.parse("2026-03-12T00:00:00Z"))
                .completedAt(Instant.parse("2026-03-12T00:00:01Z"))
                .storedAt(Instant.parse("2026-03-12T00:00:02Z"))
                .build();

        when(referenceNewsSnapshotRepository.findTopByScopeAndSnapshotDateOrderByIdDesc("GLOBAL_CRYPTO", LocalDate.parse("2026-03-12")))
                .thenReturn(Optional.of(existing));

        ReferenceNewsSnapshotGenerationFlowService service = service();
        ReferenceNewsSnapshotGenerationFlowResult result = service.generateForDateIfMissing(LocalDate.parse("2026-03-12"));

        assertThat(result.created()).isFalse();
        assertThat(result.snapshot()).isSameAs(existing);
        verify(referenceNewsGenerationService, never()).generate(any());
    }

    @Test
    void generateForDateIfMissingCreatesSnapshotWhenMissing() {
        ReferenceNewsGenerationResult generationResult = new ReferenceNewsGenerationResult(
                new ReferenceNewsPromptComposition("system", "user", "{\"scope\":\"GLOBAL_CRYPTO\"}", "{}", "{\"maxItems\":5}"),
                new ReferenceNewsGatewayResponse("{\"summary\":\"ok\",\"items\":[]}", "gpt-5-mini", "req_1", 100, 200),
                new ReferenceNewsSnapshotPayload("ok", List.of()),
                1
        );
        ReferenceNewsSnapshotDraft draft = new ReferenceNewsSnapshotDraft(
                "GLOBAL_CRYPTO",
                LocalDate.parse("2026-03-12"),
                "openai",
                "gpt-5-mini",
                "reference-news-prompt-v1",
                "reference-news-input-v1",
                "reference-news-output-v1",
                "{\"scope\":\"GLOBAL_CRYPTO\"}",
                "system",
                "user",
                "{\"maxItems\":5}",
                "{\"summary\":\"ok\",\"items\":[]}",
                new ReferenceNewsSnapshotPayload("ok", List.of()),
                "req_1",
                100,
                200,
                300,
                Instant.parse("2026-03-12T00:00:00Z"),
                Instant.parse("2026-03-12T00:00:01Z"),
                Instant.parse("2026-03-12T00:00:02Z")
        );
        ReferenceNewsSnapshotEntity saved = ReferenceNewsSnapshotEntity.builder()
                .scope("GLOBAL_CRYPTO")
                .snapshotDate(LocalDate.parse("2026-03-12"))
                .llmProvider("openai")
                .llmModel("gpt-5-mini")
                .promptTemplateVersion("reference-news-prompt-v1")
                .inputSchemaVersion("reference-news-input-v1")
                .outputSchemaVersion("reference-news-output-v1")
                .articleCount(5)
                .inputPayloadJson("{}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{}")
                .payloadJson("{\"summary\":\"ok\",\"items\":[]}")
                .requestedAt(Instant.parse("2026-03-12T00:00:00Z"))
                .completedAt(Instant.parse("2026-03-12T00:00:01Z"))
                .storedAt(Instant.parse("2026-03-12T00:00:02Z"))
                .build();

        when(referenceNewsSnapshotRepository.findTopByScopeAndSnapshotDateOrderByIdDesc("GLOBAL_CRYPTO", LocalDate.parse("2026-03-12")))
                .thenReturn(Optional.empty());
        when(referenceNewsGenerationService.generate(LocalDate.parse("2026-03-12"))).thenReturn(generationResult);
        when(referenceNewsSnapshotDraftFactory.create(any(), any(), any(), any(), any())).thenReturn(draft);
        when(referenceNewsSnapshotPersistenceService.save(draft)).thenReturn(saved);

        ReferenceNewsSnapshotGenerationFlowService service = service();
        ReferenceNewsSnapshotGenerationFlowResult result = service.generateForDateIfMissing(LocalDate.parse("2026-03-12"));

        assertThat(result.created()).isTrue();
        assertThat(result.snapshot()).isSameAs(saved);
    }

    private ReferenceNewsSnapshotGenerationFlowService service() {
        return new ReferenceNewsSnapshotGenerationFlowService(
                new ReferenceNewsProperties(
                        false,
                        "openai",
                        "GLOBAL_CRYPTO",
                        "reference-news-prompt-v1",
                        "reference-news-input-v1",
                        "reference-news-output-v1",
                        1
                ),
                referenceNewsSnapshotRepository,
                referenceNewsGenerationService,
                referenceNewsSnapshotDraftFactory,
                referenceNewsSnapshotPersistenceService,
                Clock.fixed(Instant.parse("2026-03-12T00:00:00Z"), ZoneOffset.UTC)
        );
    }
}

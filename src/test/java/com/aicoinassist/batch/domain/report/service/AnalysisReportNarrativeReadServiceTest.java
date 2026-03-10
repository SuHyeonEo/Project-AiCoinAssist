package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.controller.AnalysisReportNarrativeNotFoundException;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeView;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportNarrativeReadServiceTest {

    @Mock
    private AnalysisReportNarrativeRepository analysisReportNarrativeRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void getLatestReturnsStructuredNarrativeView() throws Exception {
        AnalysisReportNarrativeEntity entity = narrativeEntity(1L);
        when(analysisReportNarrativeRepository.findTopBySymbolAndReportTypeOrderByStoredAtDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM
        )).thenReturn(Optional.of(entity));

        AnalysisReportNarrativeView view = new AnalysisReportNarrativeReadService(
                analysisReportNarrativeRepository,
                objectMapper
        ).getLatest("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(view.id()).isEqualTo(1L);
        assertThat(view.analysisReportId()).isEqualTo(99L);
        assertThat(view.output().get("executive_conclusion")).isNotNull();
        assertThat(view.referenceNews().isArray()).isTrue();
        assertThat(view.validationIssues().isArray()).isTrue();
    }

    @Test
    void getByIdThrowsNarrativeNotFoundWhenMissing() {
        when(analysisReportNarrativeRepository.findById(123L)).thenReturn(Optional.empty());

        AnalysisReportNarrativeReadService service = new AnalysisReportNarrativeReadService(
                analysisReportNarrativeRepository,
                objectMapper
        );

        assertThatThrownBy(() -> service.getById(123L))
                .isInstanceOf(AnalysisReportNarrativeNotFoundException.class)
                .hasMessageContaining("123");
    }

    private AnalysisReportNarrativeEntity narrativeEntity(Long id) throws Exception {
        AnalysisReportEntity report = AnalysisReportEntity.builder()
                .symbol("BTCUSDT")
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .rawReferenceTime(Instant.parse("2026-03-09T00:59:30Z"))
                .sourceDataVersion("basis-key")
                .analysisEngineVersion("gpt-5.4")
                .reportPayload("{\"summary\":\"report\"}")
                .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                .build();
        Field reportId = AnalysisReportEntity.class.getDeclaredField("id");
        reportId.setAccessible(true);
        reportId.set(report, 99L);

        AnalysisReportNarrativeEntity entity = AnalysisReportNarrativeEntity.builder()
                .analysisReport(report)
                .symbol("BTCUSDT")
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .sourceDataVersion("basis-key")
                .analysisEngineVersion("gpt-5.4")
                .llmProvider("openai")
                .llmModel("gpt-5.4")
                .promptTemplateVersion("llm-prompt-v1")
                .inputSchemaVersion("llm-input-v1")
                .outputSchemaVersion("llm-output-v1")
                .inputPayloadHash("hash")
                .inputPayloadJson("{\"metadata\":{\"symbol\":\"BTCUSDT\"}}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{\"executive_conclusion\":{\"max_sentences\":3}}")
                .referenceNewsJson("[]")
                .rawOutputText("{\"raw\":true}")
                .outputJson("{\"executive_conclusion\":{\"overall_tone\":\"mixed\"}}")
                .fallbackUsed(false)
                .generationStatus(AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                .failureType(AnalysisLlmNarrativeFailureType.NONE)
                .validationIssuesJson("[]")
                .providerRequestId("req-1")
                .inputTokens(1000)
                .outputTokens(500)
                .totalTokens(1500)
                .requestedAt(Instant.parse("2026-03-09T01:00:00Z"))
                .completedAt(Instant.parse("2026-03-09T01:00:03Z"))
                .storedAt(Instant.parse("2026-03-09T01:00:05Z"))
                .build();
        Field narrativeId = AnalysisReportNarrativeEntity.class.getDeclaredField("id");
        narrativeId.setAccessible(true);
        narrativeId.set(entity, id);
        return entity;
    }
}

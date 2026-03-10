package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisGptReportInputReadServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void getLatestInputReadsStoredReportAndBuildsGptInput() throws Exception {
        AnalysisGptReportInputReadService service = new AnalysisGptReportInputReadService(
                analysisReportRepository,
                objectMapper,
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
        );
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                objectMapper.writeValueAsString(shortTermPayload("Stored summary")),
                Instant.parse("2026-03-09T01:00:30Z")
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM
        )).thenReturn(Optional.of(entity));

        AnalysisGptReportInputPayload input = service.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(input.summary().keyMessage().primaryMessage()).isEqualTo("Stored summary");
        assertThat(input.crossSignals()).isNotEmpty();
        assertThat(input.externalContextComposite().primarySignalTitle()).isEqualTo("Dollar strength regime");
    }

    @Test
    void getLatestInputThrowsWhenReportDoesNotExist() {
        AnalysisGptReportInputReadService service = new AnalysisGptReportInputReadService(
                analysisReportRepository,
                objectMapper,
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.MID_TERM
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatestInput("BTCUSDT", AnalysisReportType.MID_TERM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Analysis report not found");
    }
}

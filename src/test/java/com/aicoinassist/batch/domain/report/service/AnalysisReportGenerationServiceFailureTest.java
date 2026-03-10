package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportGenerationServiceFailureTest extends AnalysisReportGenerationServiceTestSupport {

    @Test
    void generateAndSaveFailsWhenNoSnapshotExistsForMappedInterval() {
        AnalysisReportGenerationService service = createService();

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "1d"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("symbol=BTCUSDT")
          .hasMessageContaining("interval=1d");
    }
}

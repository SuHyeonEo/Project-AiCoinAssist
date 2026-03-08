package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchServiceTest {

    @Mock
    private MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;

    @Mock
    private AnalysisReportGenerationService analysisReportGenerationService;

    @Test
    void generateForSymbolCreatesSnapshotsBeforeReportsForAllHorizons() {
        AnalysisReportBatchService service = new AnalysisReportBatchService(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService
        );

        Instant storedTime = Instant.parse("2026-03-09T01:00:30Z");

        AnalysisReportBatchResult result = service.generateForSymbol("BTCUSDT", "report-assembler-v1", storedTime);

        InOrder inOrder = inOrder(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService
        );
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.ONE_HOUR);
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.FOUR_HOUR);
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.ONE_DAY);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.SHORT_TERM, "report-assembler-v1", storedTime);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.MID_TERM, "report-assembler-v1", storedTime);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.LONG_TERM, "report-assembler-v1", storedTime);

        assertThat(result.symbol()).isEqualTo("BTCUSDT");
        assertThat(result.snapshotCount()).isEqualTo(3);
        assertThat(result.reportCount()).isEqualTo(3);
    }
}

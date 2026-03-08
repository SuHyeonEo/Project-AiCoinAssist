package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchSchedulerTest {

    @Mock
    private AnalysisReportBatchExecutionService analysisReportBatchExecutionService;

    @Test
    void runDelegatesToExecutionServiceWithScheduledTrigger() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v1",
                List.of(AssetType.BTC, AssetType.ETH),
                List.of(AnalysisReportType.SHORT_TERM),
                300000L
        );
        AnalysisReportBatchScheduler scheduler = new AnalysisReportBatchScheduler(
                analysisReportBatchExecutionService,
                properties
        );

        when(analysisReportBatchExecutionService.execute(
                properties.assetTypes(),
                "report-assembler-v1",
                BatchExecutionTriggerType.SCHEDULED,
                null
        )).thenReturn(new AnalysisReportBatchRunResult(
                "run-001",
                BatchExecutionTriggerType.SCHEDULED,
                null,
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:10Z"),
                10000L,
                List.of(
                        AnalysisReportBatchResult.crashed(
                                "run-001",
                                "ETHUSDT",
                                Instant.parse("2026-03-09T01:00:01Z"),
                                Instant.parse("2026-03-09T01:00:03Z"),
                                "crash"
                        )
                )
        ));

        scheduler.run();

        verify(analysisReportBatchExecutionService).execute(
                properties.assetTypes(),
                "report-assembler-v1",
                BatchExecutionTriggerType.SCHEDULED,
                null
        );
    }
}

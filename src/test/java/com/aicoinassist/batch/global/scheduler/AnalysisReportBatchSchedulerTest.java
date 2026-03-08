package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchRunPersistenceService;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchSchedulerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-09T01:00:30Z"),
            ZoneOffset.UTC
    );

    @Mock
    private AnalysisReportBatchService analysisReportBatchService;

    @Mock
    private AnalysisReportBatchRunPersistenceService analysisReportBatchRunPersistenceService;

    @Test
    void runPersistsRunResultIncludingUnexpectedAssetCrash() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v1",
                List.of(AssetType.BTC, AssetType.ETH),
                List.of(AnalysisReportType.SHORT_TERM),
                300000L
        );
        AnalysisReportBatchScheduler scheduler = new AnalysisReportBatchScheduler(
                analysisReportBatchService,
                analysisReportBatchRunPersistenceService,
                properties,
                FIXED_CLOCK
        );

        when(analysisReportBatchService.generateForAsset(eq(AssetType.BTC), any(), eq(Instant.parse("2026-03-09T01:00:30Z"))))
                .thenAnswer(invocation -> new AnalysisReportBatchResult(
                        invocation.getArgument(1),
                        "BTCUSDT",
                        Instant.parse("2026-03-09T01:00:30Z"),
                        Instant.parse("2026-03-09T01:00:30Z"),
                        0L,
                        List.of(new AnalysisReportSnapshotStepResult(CandleInterval.ONE_HOUR, true, null)),
                        List.of(new AnalysisReportStepResult(AnalysisReportType.SHORT_TERM, true, null)),
                        null
                ));
        when(analysisReportBatchService.generateForAsset(eq(AssetType.ETH), any(), eq(Instant.parse("2026-03-09T01:00:30Z"))))
                .thenThrow(new IllegalStateException("scheduler path crash"));

        scheduler.run();

        ArgumentCaptor<AnalysisReportBatchRunResult> runCaptor = ArgumentCaptor.forClass(AnalysisReportBatchRunResult.class);

        verify(analysisReportBatchRunPersistenceService).save(
                runCaptor.capture(),
                eq("report-assembler-v1"),
                eq(Instant.parse("2026-03-09T01:00:30Z"))
        );

        AnalysisReportBatchRunResult runResult = runCaptor.getValue();
        assertThat(runResult.runId()).isNotBlank();
        assertThat(runResult.assetResults()).hasSize(2);
        assertThat(runResult.assetSuccessCount()).isEqualTo(1);
        assertThat(runResult.assetFailureCount()).isEqualTo(1);
        assertThat(runResult.status().name()).isEqualTo("PARTIAL_FAILURE");
        assertThat(runResult.assetResults().get(1).symbol()).isEqualTo("ETHUSDT");
        assertThat(runResult.assetResults().get(1).crashed()).isTrue();
        assertThat(runResult.assetResults().get(1).crashErrorMessage()).contains("scheduler path crash");
    }
}

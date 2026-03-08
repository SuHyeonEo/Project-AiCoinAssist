package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
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
class AnalysisReportBatchExecutionServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-09T01:00:30Z"),
            ZoneOffset.UTC
    );

    @Mock
    private AnalysisReportBatchService analysisReportBatchService;

    @Mock
    private AnalysisReportBatchRunPersistenceService analysisReportBatchRunPersistenceService;

    @Test
    void executePersistsRunAndCapturesUnexpectedAssetCrash() {
        AnalysisReportBatchExecutionService service = new AnalysisReportBatchExecutionService(
                analysisReportBatchService,
                analysisReportBatchRunPersistenceService,
                FIXED_CLOCK
        );

        when(analysisReportBatchService.generateForAsset(eq(AssetType.BTC), any(), eq("report-assembler-v1"), eq(Instant.parse("2026-03-09T01:00:30Z"))))
                .thenAnswer(invocation -> new AnalysisReportBatchResult(
                        invocation.getArgument(1),
                        "BTCUSDT",
                        Instant.parse("2026-03-09T01:00:30Z"),
                        Instant.parse("2026-03-09T01:00:30Z"),
                        0L,
                        List.of(),
                        List.of(),
                        null
                ));
        when(analysisReportBatchService.generateForAsset(eq(AssetType.ETH), any(), eq("report-assembler-v1"), eq(Instant.parse("2026-03-09T01:00:30Z"))))
                .thenThrow(new IllegalStateException("scheduler path crash"));

        AnalysisReportBatchRunResult runResult = service.execute(
                List.of(AssetType.BTC, AssetType.ETH),
                "report-assembler-v1",
                BatchExecutionTriggerType.SCHEDULED,
                null
        );

        ArgumentCaptor<AnalysisReportBatchRunResult> runCaptor = ArgumentCaptor.forClass(AnalysisReportBatchRunResult.class);

        verify(analysisReportBatchRunPersistenceService).save(
                runCaptor.capture(),
                eq("report-assembler-v1"),
                eq(Instant.parse("2026-03-09T01:00:30Z"))
        );

        assertThat(runResult.runId()).isNotBlank();
        assertThat(runResult.triggerType()).isEqualTo(BatchExecutionTriggerType.SCHEDULED);
        assertThat(runResult.assetSuccessCount()).isEqualTo(1);
        assertThat(runResult.assetFailureCount()).isEqualTo(1);
        assertThat(runResult.assetResults().get(1).crashed()).isTrue();
        assertThat(runResult.assetResults().get(1).crashErrorMessage()).contains("scheduler path crash");
        assertThat(runCaptor.getValue()).isEqualTo(runResult);
    }
}

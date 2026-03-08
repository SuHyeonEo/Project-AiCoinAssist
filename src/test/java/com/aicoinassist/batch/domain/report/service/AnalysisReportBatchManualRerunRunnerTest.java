package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchManualRerunProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchManualRerunRunnerTest {

    @Mock
    private AnalysisReportBatchRerunService analysisReportBatchRerunService;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Test
    void runTriggersRerunAndClosesContextWhenConfigured() throws Exception {
        AnalysisReportBatchManualRerunProperties properties = new AnalysisReportBatchManualRerunProperties(
                true,
                "run-001",
                true
        );
        AnalysisReportBatchManualRerunRunner runner = new AnalysisReportBatchManualRerunRunner(
                properties,
                analysisReportBatchRerunService,
                applicationContext
        );

        when(analysisReportBatchRerunService.rerunFailedAssets("run-001")).thenReturn(
                new AnalysisReportBatchRunResult(
                        "run-002",
                        BatchExecutionTriggerType.MANUAL_RERUN,
                        "run-001",
                        Instant.parse("2026-03-09T01:00:00Z"),
                        Instant.parse("2026-03-09T01:00:05Z"),
                        5000L,
                        List.of()
                )
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(analysisReportBatchRerunService).rerunFailedAssets("run-001");
        verify(applicationContext).close();
    }

    @Test
    void runKeepsContextOpenWhenShutdownDisabled() throws Exception {
        AnalysisReportBatchManualRerunProperties properties = new AnalysisReportBatchManualRerunProperties(
                true,
                "run-003",
                false
        );
        AnalysisReportBatchManualRerunRunner runner = new AnalysisReportBatchManualRerunRunner(
                properties,
                analysisReportBatchRerunService,
                applicationContext
        );

        when(analysisReportBatchRerunService.rerunFailedAssets("run-003")).thenReturn(
                new AnalysisReportBatchRunResult(
                        "run-004",
                        BatchExecutionTriggerType.MANUAL_RERUN,
                        "run-003",
                        Instant.parse("2026-03-09T01:10:00Z"),
                        Instant.parse("2026-03-09T01:10:05Z"),
                        5000L,
                        List.of()
                )
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(analysisReportBatchRerunService).rerunFailedAssets("run-003");
        verify(applicationContext, never()).close();
    }
}

package com.aicoinassist.batch.domain.report.controller;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchAssetResultView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunDetailView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunSummaryView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiAuthInterceptor;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiProperties;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiWebConfig;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchRerunService;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchRunReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalysisReportBatchAdminController.class)
@EnableConfigurationProperties(AnalysisReportBatchAdminApiProperties.class)
@Import({
        AnalysisReportBatchAdminExceptionHandler.class,
        AnalysisReportBatchAdminApiWebConfig.class,
        AnalysisReportBatchAdminApiAuthInterceptor.class
})
@TestPropertySource(properties = {
        "batch.admin-api.enabled=true",
        "batch.admin-api.token=test-admin-token",
        "batch.admin-api.header-name=X-Admin-Token"
})
class AnalysisReportBatchAdminControllerTest {

    private static final String ADMIN_HEADER = "X-Admin-Token";
    private static final String ADMIN_TOKEN = "test-admin-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisReportBatchRunReadService analysisReportBatchRunReadService;

    @MockitoBean
    private AnalysisReportBatchRerunService analysisReportBatchRerunService;

    @Test
    void listRecentRunsRejectsRequestWithoutAdminToken() throws Exception {
        mockMvc.perform(get("/internal/admin/report-batch-runs")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.title").value("Unauthorized admin API access"));
    }

    @Test
    void listRecentRunsReturnsSummaryList() throws Exception {
        when(analysisReportBatchRunReadService.listRecentRuns(10)).thenReturn(List.of(
                new AnalysisReportBatchRunSummaryView(
                        "run-001",
                        BatchExecutionStatus.PARTIAL_FAILURE,
                        BatchExecutionTriggerType.SCHEDULED,
                        null,
                        "report-assembler-v1",
                        Instant.parse("2026-03-09T01:00:00Z"),
                        Instant.parse("2026-03-09T01:00:10Z"),
                        10000L,
                        1,
                        2,
                        true
                )
        ));

        mockMvc.perform(get("/internal/admin/report-batch-runs")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].runId").value("run-001"))
               .andExpect(jsonPath("$[0].executionStatus").value("PARTIAL_FAILURE"))
               .andExpect(jsonPath("$[0].rerunnable").value(true));
    }

    @Test
    void getRunDetailReturnsFullDetail() throws Exception {
        when(analysisReportBatchRunReadService.getRunDetail("run-001")).thenReturn(
                new AnalysisReportBatchRunDetailView(
                        "run-001",
                        BatchExecutionStatus.PARTIAL_FAILURE,
                        BatchExecutionTriggerType.SCHEDULED,
                        null,
                        "report-assembler-v1",
                        Instant.parse("2026-03-09T01:00:00Z"),
                        Instant.parse("2026-03-09T01:00:10Z"),
                        10000L,
                        1,
                        2,
                        Instant.parse("2026-03-09T01:00:10Z"),
                        true,
                        List.of("ETHUSDT", "XRPUSDT"),
                        List.of(
                                new AnalysisReportBatchAssetResultView(
                                        "ETHUSDT",
                                        BatchExecutionStatus.PARTIAL_FAILURE,
                                        Instant.parse("2026-03-09T01:00:00Z"),
                                        Instant.parse("2026-03-09T01:00:05Z"),
                                        5000L,
                                        1,
                                        0,
                                        0,
                                        1,
                                        null,
                                        List.of(new AnalysisReportSnapshotStepResult(com.aicoinassist.batch.domain.market.enumtype.CandleInterval.ONE_HOUR, true, null)),
                                        List.of(new AnalysisReportStepResult(AnalysisReportType.SHORT_TERM, false, "report failed"))
                                )
                        )
                )
        );

        mockMvc.perform(get("/internal/admin/report-batch-runs/run-001")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.runId").value("run-001"))
               .andExpect(jsonPath("$.rerunnableSymbols[0]").value("ETHUSDT"))
               .andExpect(jsonPath("$.assetResults[0].reportResults[0].errorMessage").value("report failed"));
    }

    @Test
    void rerunFailedAssetsReturnsNewRunResult() throws Exception {
        when(analysisReportBatchRerunService.rerunFailedAssets("run-001")).thenReturn(
                new AnalysisReportBatchRunResult(
                        "run-002",
                        BatchExecutionTriggerType.MANUAL_RERUN,
                        "run-001",
                        Instant.parse("2026-03-09T02:00:00Z"),
                        Instant.parse("2026-03-09T02:00:05Z"),
                        5000L,
                        List.of(
                                new AnalysisReportBatchResult(
                                        "run-002",
                                        "ETHUSDT",
                                        Instant.parse("2026-03-09T02:00:00Z"),
                                        Instant.parse("2026-03-09T02:00:05Z"),
                                        5000L,
                                        List.of(),
                                        List.of(),
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(post("/internal/admin/report-batch-runs/run-001/rerun-failed")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.runId").value("run-002"))
               .andExpect(jsonPath("$.triggerType").value("MANUAL_RERUN"))
               .andExpect(jsonPath("$.rerunSourceRunId").value("run-001"));
    }

    @Test
    void getRunDetailReturnsNotFoundWhenRunIdIsUnknown() throws Exception {
        when(analysisReportBatchRunReadService.getRunDetail("missing-run"))
                .thenThrow(new IllegalArgumentException("Batch run not found: missing-run"));

        mockMvc.perform(get("/internal/admin/report-batch-runs/missing-run")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.title").value("Batch run not found"));
    }

    @Test
    void rerunFailedAssetsReturnsConflictWhenNothingCanBeRerun() throws Exception {
        when(analysisReportBatchRerunService.rerunFailedAssets("run-001"))
                .thenThrow(new IllegalStateException("No failed asset results found for runId: run-001"));

        mockMvc.perform(post("/internal/admin/report-batch-runs/run-001/rerun-failed")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.title").value("Batch rerun not available"));
    }
}

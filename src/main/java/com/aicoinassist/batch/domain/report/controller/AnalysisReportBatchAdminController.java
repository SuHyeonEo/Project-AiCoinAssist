package com.aicoinassist.batch.domain.report.controller;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunDetailView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunSummaryView;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchRerunService;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchRunReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/report-batch-runs")
public class AnalysisReportBatchAdminController {

    private final AnalysisReportBatchRunReadService analysisReportBatchRunReadService;
    private final AnalysisReportBatchRerunService analysisReportBatchRerunService;

    @GetMapping
    public List<AnalysisReportBatchRunSummaryView> listRecentRuns(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return analysisReportBatchRunReadService.listRecentRuns(limit);
    }

    @GetMapping("/{runId}")
    public AnalysisReportBatchRunDetailView getRunDetail(@PathVariable String runId) {
        return analysisReportBatchRunReadService.getRunDetail(runId);
    }

    @PostMapping("/{runId}/rerun-failed")
    public AnalysisReportBatchRunResult rerunFailedAssets(@PathVariable String runId) {
        return analysisReportBatchRerunService.rerunFailedAssets(runId);
    }
}

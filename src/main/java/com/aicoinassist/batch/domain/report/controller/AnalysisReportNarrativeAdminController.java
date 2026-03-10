package com.aicoinassist.batch.domain.report.controller;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeView;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportNarrativeReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/report-narratives")
public class AnalysisReportNarrativeAdminController {

    private final AnalysisReportNarrativeReadService analysisReportNarrativeReadService;

    @GetMapping("/latest")
    public AnalysisReportNarrativeView getLatestNarrative(
            @RequestParam String symbol,
            @RequestParam AnalysisReportType reportType
    ) {
        return analysisReportNarrativeReadService.getLatest(symbol, reportType);
    }

    @GetMapping("/{id}")
    public AnalysisReportNarrativeView getNarrativeDetail(@PathVariable Long id) {
        return analysisReportNarrativeReadService.getById(id);
    }
}

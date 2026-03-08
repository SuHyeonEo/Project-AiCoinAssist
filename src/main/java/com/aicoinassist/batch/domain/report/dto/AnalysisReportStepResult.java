package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

public record AnalysisReportStepResult(
        AnalysisReportType reportType,
        boolean success,
        String errorMessage
) {
}

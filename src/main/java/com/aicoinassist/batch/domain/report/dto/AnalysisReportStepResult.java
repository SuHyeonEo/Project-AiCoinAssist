package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

public record AnalysisReportStepResult(
        AnalysisReportType reportType,
        boolean success,
        String errorMessage,
        AnalysisLlmNarrativeGenerationStatus narrativeGenerationStatus,
        Boolean narrativeFallbackUsed,
        AnalysisLlmNarrativeFailureType narrativeFailureType,
        String narrativeErrorMessage
) {

    public AnalysisReportStepResult(
            AnalysisReportType reportType,
            boolean success,
            String errorMessage
    ) {
        this(reportType, success, errorMessage, null, null, null, null);
    }
}

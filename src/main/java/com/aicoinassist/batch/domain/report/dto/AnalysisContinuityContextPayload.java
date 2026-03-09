package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

public record AnalysisContinuityContextPayload(
        AnalysisComparisonReference reference,
        String summary
) {
}

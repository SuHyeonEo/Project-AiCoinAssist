package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

public record AnalysisComparisonHighlight(
        AnalysisComparisonReference reference,
        String headline,
        String detail
) {
}

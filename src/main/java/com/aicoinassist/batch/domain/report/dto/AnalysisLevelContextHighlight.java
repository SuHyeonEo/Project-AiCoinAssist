package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

public record AnalysisLevelContextHighlight(
        AnalysisComparisonReference reference,
        String headline,
        String detail
) {
}

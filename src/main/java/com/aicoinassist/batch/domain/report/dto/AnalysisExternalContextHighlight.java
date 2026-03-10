package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;

public record AnalysisExternalContextHighlight(
        String title,
        String summary,
        AnalysisContextHeadlineImportance importance,
        AnalysisComparisonReference reference
) {
}

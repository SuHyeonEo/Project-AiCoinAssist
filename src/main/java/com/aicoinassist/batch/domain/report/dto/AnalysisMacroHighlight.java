package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisMacroHighlightImportance;

public record AnalysisMacroHighlight(
        String title,
        String summary,
        AnalysisMacroHighlightImportance importance,
        AnalysisComparisonReference reference
) {
}

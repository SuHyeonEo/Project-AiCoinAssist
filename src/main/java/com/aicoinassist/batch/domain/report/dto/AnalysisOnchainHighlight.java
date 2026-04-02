package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOnchainHighlightImportance;

public record AnalysisOnchainHighlight(
        String title,
        String summary,
        AnalysisOnchainHighlightImportance importance,
        AnalysisComparisonReference reference
) {
}

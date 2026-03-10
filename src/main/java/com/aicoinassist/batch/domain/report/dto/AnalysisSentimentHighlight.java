package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisSentimentHighlightImportance;

public record AnalysisSentimentHighlight(
        String title,
        String summary,
        AnalysisSentimentHighlightImportance importance,
        AnalysisComparisonReference reference
) {
}

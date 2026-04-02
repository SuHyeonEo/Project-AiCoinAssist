package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;

public record AnalysisDerivativeHighlight(
        String title,
        String summary,
        AnalysisDerivativeHighlightImportance importance,
        AnalysisDerivativeMetricType relatedMetric,
        AnalysisComparisonReference reference,
        MarketWindowType windowType
) {
}

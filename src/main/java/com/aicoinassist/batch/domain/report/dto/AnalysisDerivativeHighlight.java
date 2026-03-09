package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

public record AnalysisDerivativeHighlight(
        String title,
        String summary,
        String importance,
        String relatedMetric,
        AnalysisComparisonReference reference,
        MarketWindowType windowType
) {
}

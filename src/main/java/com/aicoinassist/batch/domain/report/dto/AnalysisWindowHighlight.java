package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

public record AnalysisWindowHighlight(
        MarketWindowType windowType,
        String headline,
        String detail
) {
}

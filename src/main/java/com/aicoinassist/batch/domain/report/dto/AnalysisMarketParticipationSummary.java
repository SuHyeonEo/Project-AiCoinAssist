package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisMarketParticipationSummary(
        String windowLabel,
        Instant currentWindowStartTime,
        Instant currentWindowEndTime,
        Instant previousWindowStartTime,
        Instant previousWindowEndTime,
        Integer sampleCount,
        BigDecimal priceChangeRate,
        BigDecimal quoteVolumeChangeRate,
        BigDecimal tradeCountChangeRate,
        BigDecimal takerBuyQuoteRatio,
        BigDecimal takerBuyQuoteRatioDelta
) {
}

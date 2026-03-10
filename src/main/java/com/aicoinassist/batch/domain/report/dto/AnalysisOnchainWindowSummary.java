package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisOnchainWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal averageActiveAddressCount,
        BigDecimal currentActiveAddressVsAverage,
        BigDecimal averageTransactionCount,
        BigDecimal currentTransactionCountVsAverage,
        BigDecimal averageMarketCapUsd,
        BigDecimal currentMarketCapVsAverage
) {
}

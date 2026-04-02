package com.aicoinassist.batch.domain.onchain.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record OnchainWindowSummarySnapshot(
        String symbol,
        String assetCode,
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal currentActiveAddressCount,
        BigDecimal averageActiveAddressCount,
        BigDecimal currentActiveAddressVsAverage,
        BigDecimal currentTransactionCount,
        BigDecimal averageTransactionCount,
        BigDecimal currentTransactionCountVsAverage,
        BigDecimal currentMarketCapUsd,
        BigDecimal averageMarketCapUsd,
        BigDecimal currentMarketCapVsAverage,
        String sourceDataVersion
) {
}

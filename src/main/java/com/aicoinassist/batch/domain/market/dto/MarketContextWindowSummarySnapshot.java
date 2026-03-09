package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketContextWindowSummarySnapshot(
        String symbol,
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        int sampleCount,
        BigDecimal currentOpenInterest,
        BigDecimal averageOpenInterest,
        BigDecimal currentOpenInterestVsAverage,
        BigDecimal currentFundingRate,
        BigDecimal averageFundingRate,
        BigDecimal currentFundingVsAverage,
        BigDecimal currentBasisRate,
        BigDecimal averageBasisRate,
        BigDecimal currentBasisVsAverage,
        String sourceDataVersion
) {
}

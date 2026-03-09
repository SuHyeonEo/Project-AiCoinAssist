package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketWindowSummarySnapshot(
        String symbol,
        String intervalValue,
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        int sampleCount,
        BigDecimal currentPrice,
        BigDecimal windowHigh,
        BigDecimal windowLow,
        BigDecimal windowRange,
        BigDecimal currentPositionInRange,
        BigDecimal distanceFromWindowHigh,
        BigDecimal reboundFromWindowLow,
        BigDecimal averageVolume,
        BigDecimal averageAtr,
        BigDecimal currentVolume,
        BigDecimal currentAtr,
        BigDecimal currentVolumeVsAverage,
        BigDecimal currentAtrVsAverage,
        String sourceDataVersion
) {
}

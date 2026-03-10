package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketExternalContextWindowSummarySnapshot(
        String symbol,
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal currentCompositeRiskScore,
        BigDecimal averageCompositeRiskScore,
        BigDecimal currentCompositeRiskVsAverage,
        Integer supportiveDominanceSampleCount,
        Integer cautionaryDominanceSampleCount,
        Integer headwindDominanceSampleCount,
        Integer highSeveritySampleCount,
        String sourceDataVersion
) {
}

package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        int sampleCount,
        BigDecimal high,
        BigDecimal low,
        BigDecimal range,
        BigDecimal currentPositionInRange,
        BigDecimal distanceFromWindowHigh,
        BigDecimal reboundFromWindowLow,
        BigDecimal averageVolume,
        BigDecimal averageAtr,
        BigDecimal currentVolumeVsAverage,
        BigDecimal currentAtrVsAverage
) {
}

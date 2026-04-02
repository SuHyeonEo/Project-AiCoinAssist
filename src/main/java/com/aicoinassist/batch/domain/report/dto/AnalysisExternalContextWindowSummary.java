package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisExternalContextWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal averageCompositeRiskScore,
        BigDecimal currentCompositeRiskVsAverage,
        Integer supportiveDominanceSampleCount,
        Integer cautionaryDominanceSampleCount,
        Integer headwindDominanceSampleCount,
        Integer highSeveritySampleCount
) {
}

package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisDerivativeWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        int sampleCount,
        BigDecimal averageOpenInterest,
        BigDecimal currentOpenInterestVsAverage,
        BigDecimal averageFundingRate,
        BigDecimal currentFundingVsAverage,
        BigDecimal averageBasisRate,
        BigDecimal currentBasisVsAverage
) {
}

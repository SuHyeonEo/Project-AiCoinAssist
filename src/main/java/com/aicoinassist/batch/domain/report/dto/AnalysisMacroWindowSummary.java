package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisMacroWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal averageDxyProxyValue,
        BigDecimal currentDxyProxyVsAverage,
        BigDecimal averageUs10yYieldValue,
        BigDecimal currentUs10yYieldVsAverage,
        BigDecimal averageUsdKrwValue,
        BigDecimal currentUsdKrwVsAverage
) {
}

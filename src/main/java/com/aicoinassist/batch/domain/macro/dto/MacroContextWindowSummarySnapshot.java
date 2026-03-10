package com.aicoinassist.batch.domain.macro.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record MacroContextWindowSummarySnapshot(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal currentDxyProxyValue,
        BigDecimal averageDxyProxyValue,
        BigDecimal currentDxyProxyVsAverage,
        BigDecimal currentUs10yYieldValue,
        BigDecimal averageUs10yYieldValue,
        BigDecimal currentUs10yYieldVsAverage,
        BigDecimal currentUsdKrwValue,
        BigDecimal averageUsdKrwValue,
        BigDecimal currentUsdKrwVsAverage,
        String sourceDataVersion
) {
}

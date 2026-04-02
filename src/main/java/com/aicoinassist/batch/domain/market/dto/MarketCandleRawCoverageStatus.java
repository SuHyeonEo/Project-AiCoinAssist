package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;

import java.time.Instant;

public record MarketCandleRawCoverageStatus(
        String symbol,
        CandleInterval interval,
        Instant expectedLatestOpenTime,
        Instant latestStoredOpenTime,
        Instant requiredWindowStartOpenTime,
        int requiredCandleCount,
        long availableValidCandleCount,
        int missingCandleCount,
        int tailGapCandleCount,
        boolean sufficientlyCovered
) {
}


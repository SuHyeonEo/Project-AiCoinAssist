package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;

import java.time.Instant;

public record MarketRawIngestionResult(
        String symbol,
        CandleInterval interval,
        Instant collectedTime,
        RawDataValidationStatus priceValidationStatus,
        int candleCount,
        int invalidCandleCount
) {
}

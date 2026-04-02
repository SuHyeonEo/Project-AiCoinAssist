package com.aicoinassist.batch.domain.market.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record Candle(
        Instant openTime,
        Instant closeTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
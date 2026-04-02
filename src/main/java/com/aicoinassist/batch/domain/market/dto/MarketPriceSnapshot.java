package com.aicoinassist.batch.domain.market.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceSnapshot(
        String symbol,
        BigDecimal price,
        Instant sourceEventTime
) {
}

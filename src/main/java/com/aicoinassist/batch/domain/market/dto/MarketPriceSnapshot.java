package com.aicoinassist.batch.domain.market.dto;

import java.math.BigDecimal;

public record MarketPriceSnapshot(
        String symbol,
        BigDecimal price
) {
}
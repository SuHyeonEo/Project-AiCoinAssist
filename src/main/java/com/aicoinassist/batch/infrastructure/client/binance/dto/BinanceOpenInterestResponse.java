package com.aicoinassist.batch.infrastructure.client.binance.dto;

public record BinanceOpenInterestResponse(
        String openInterest,
        String symbol,
        Long time
) {
}

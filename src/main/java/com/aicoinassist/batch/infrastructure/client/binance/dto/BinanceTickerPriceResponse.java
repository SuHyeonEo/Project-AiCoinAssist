package com.aicoinassist.batch.infrastructure.client.binance.dto;

public record BinanceTickerPriceResponse(
        String symbol,
        String price
) {
}
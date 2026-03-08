package com.aicoinassist.batch.infrastructure.client.binance.dto;

import java.util.List;

public record BinanceKlineResponse(
        Long openTime,
        String open,
        String high,
        String low,
        String close,
        String volume,
        Long closeTime,
        String quoteAssetVolume,
        Long numberOfTrades,
        String takerBuyBaseAssetVolume,
        String takerBuyQuoteAssetVolume,
        String ignoreValue,
        List<String> rawValues
) {
}

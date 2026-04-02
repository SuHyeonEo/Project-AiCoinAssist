package com.aicoinassist.batch.infrastructure.client.binance.dto;

public record BinancePremiumIndexResponse(
        String symbol,
        String markPrice,
        String indexPrice,
        String estimatedSettlePrice,
        String lastFundingRate,
        String interestRate,
        Long nextFundingTime,
        Long time
) {
}

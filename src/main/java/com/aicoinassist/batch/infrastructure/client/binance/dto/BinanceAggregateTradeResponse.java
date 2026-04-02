package com.aicoinassist.batch.infrastructure.client.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BinanceAggregateTradeResponse(
        @JsonProperty("a") Long aggregateTradeId,
        @JsonProperty("p") String price,
        @JsonProperty("q") String quantity,
        @JsonProperty("f") Long firstTradeId,
        @JsonProperty("l") Long lastTradeId,
        @JsonProperty("T") Long tradeTime,
        @JsonProperty("m") Boolean buyerMaker,
        @JsonProperty("M") Boolean bestPriceMatch
) {
}

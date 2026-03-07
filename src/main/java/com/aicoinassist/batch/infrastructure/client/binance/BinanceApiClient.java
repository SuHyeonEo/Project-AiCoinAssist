package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.global.config.BinanceProperties;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BinanceApiClient {

    private final RestClient restClient;
    private final BinanceProperties binanceProperties;

    public BinanceTickerPriceResponse getTickerPrice(String symbol) {
        return restClient.get()
                         .uri(binanceProperties.baseUrl() + "/api/v3/ticker/price?symbol={symbol}", symbol)
                         .retrieve()
                         .body(BinanceTickerPriceResponse.class);
    }
}
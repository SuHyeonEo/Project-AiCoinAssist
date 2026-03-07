package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.global.config.BinanceProperties;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    public List<List<Object>> getKlines(String symbol, String interval, int limit) {
        return restClient.get()
                         .uri(
                                 binanceProperties.baseUrl() + "/api/v3/klines?symbol={symbol}&interval={interval}&limit={limit}",
                                 symbol, interval, limit
                         )
                         .retrieve()
                         .body(new ParameterizedTypeReference<>() {});
    }
}
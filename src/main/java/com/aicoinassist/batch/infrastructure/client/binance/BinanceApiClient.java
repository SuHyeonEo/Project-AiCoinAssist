package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.global.config.BinanceProperties;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
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

    public BinanceAggregateTradeResponse getLatestAggregateTrade(String symbol) {
        List<BinanceAggregateTradeResponse> response = restClient.get()
                                                                .uri(
                                                                        binanceProperties.baseUrl() + "/api/v3/aggTrades?symbol={symbol}&limit=1",
                                                                        symbol
                                                                )
                                                                .retrieve()
                                                                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.isEmpty()) {
            return null;
        }

        return response.get(0);
    }

    public List<BinanceKlineResponse> getKlines(String symbol, String interval, int limit) {
        List<List<Object>> response = restClient.get()
                                                .uri(
                                                        binanceProperties.baseUrl() + "/api/v3/klines?symbol={symbol}&interval={interval}&limit={limit}",
                                                        symbol, interval, limit
                                                )
                                                .retrieve()
                                                .body(new ParameterizedTypeReference<>() {});

        if (response == null) {
            return List.of();
        }

        return response.stream()
                       .map(this::toKlineResponse)
                       .toList();
    }

    private BinanceKlineResponse toKlineResponse(List<Object> item) {
        List<String> rawValues = item == null
                ? List.of()
                : item.stream()
                      .map(value -> value == null ? null : value.toString())
                      .toList();

        return new BinanceKlineResponse(
                toLong(item, 0),
                toStringValue(item, 1),
                toStringValue(item, 2),
                toStringValue(item, 3),
                toStringValue(item, 4),
                toStringValue(item, 5),
                toLong(item, 6),
                toStringValue(item, 7),
                toLong(item, 8),
                toStringValue(item, 9),
                toStringValue(item, 10),
                toStringValue(item, 11),
                rawValues
        );
    }

    private Long toLong(List<Object> item, int index) {
        Object value = get(item, index);
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toStringValue(List<Object> item, int index) {
        Object value = get(item, index);
        return value == null ? null : value.toString();
    }

    private Object get(List<Object> item, int index) {
        if (item == null || index >= item.size()) {
            return null;
        }

        return item.get(index);
    }
}

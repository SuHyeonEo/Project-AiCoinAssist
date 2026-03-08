package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BinanceMarketClient {

    private final BinanceApiClient binanceApiClient;

    public MarketPriceSnapshot getCurrentPrice(String symbol) {
        BinanceTickerPriceResponse response = binanceApiClient.getTickerPrice(symbol);

        return new MarketPriceSnapshot(
                response.symbol(),
                new BigDecimal(response.price())
        );
    }

    public List<Candle> getCandles(String symbol, CandleInterval interval, int limit) {
        List<BinanceKlineResponse> response = binanceApiClient.getKlines(symbol, interval.value(), limit);

        return response.stream()
                       .map(this::toCandle)
                       .toList();
    }

    private Candle toCandle(BinanceKlineResponse item) {
        return new Candle(
                Instant.ofEpochMilli(item.openTime()),
                Instant.ofEpochMilli(item.closeTime()),
                new BigDecimal(item.open()),
                new BigDecimal(item.high()),
                new BigDecimal(item.low()),
                new BigDecimal(item.close()),
                new BigDecimal(item.volume())
        );
    }
}

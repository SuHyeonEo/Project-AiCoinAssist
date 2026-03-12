package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceTickerPriceResponseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BinanceMarketClient {

    private static final int CURRENT_PRICE_MAX_ATTEMPTS = 3;

    private final BinanceApiClient binanceApiClient;
    private final BinanceTickerPriceResponseValidator tickerPriceResponseValidator;
    private final Clock clock;

    public MarketPriceSnapshot getCurrentPrice(String symbol) {
        RuntimeException lastException = null;

        for (int attempt = 0; attempt < CURRENT_PRICE_MAX_ATTEMPTS; attempt++) {
            try {
                MarketPriceSnapshot snapshot = toAggregateTradeSnapshot(symbol, binanceApiClient.getLatestAggregateTrade(symbol));
                if (snapshot != null) {
                    return snapshot;
                }
            } catch (RuntimeException exception) {
                lastException = exception;
            }
        }

        return getTickerPriceFallback(symbol, lastException);
    }

    public List<Candle> getCandles(String symbol, CandleInterval interval, int limit) {
        List<BinanceKlineResponse> response = binanceApiClient.getKlines(symbol, interval.value(), limit);

        return response.stream()
                       .map(this::toCandle)
                       .toList();
    }

    public List<Candle> getClosedCandles(String symbol, CandleInterval interval, int limit) {
        Instant now = clock.instant();
        List<Candle> closedCandles = getCandles(symbol, interval, limit + 1).stream()
                .filter(candle -> !candle.closeTime().isAfter(now))
                .toList();

        int skipCount = Math.max(0, closedCandles.size() - limit);
        return closedCandles.stream()
                .skip(skipCount)
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

    private MarketPriceSnapshot toAggregateTradeSnapshot(String symbol, BinanceAggregateTradeResponse response) {
        if (response == null || response.tradeTime() == null) {
            return null;
        }

        BigDecimal price = parseDecimal(response.price());
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        return new MarketPriceSnapshot(
                symbol,
                price,
                Instant.ofEpochMilli(response.tradeTime())
        );
    }

    private MarketPriceSnapshot getTickerPriceFallback(String symbol, RuntimeException lastException) {
        try {
            BinanceTickerPriceResponse response = binanceApiClient.getTickerPrice(symbol);
            RawDataValidationResult validationResult = tickerPriceResponseValidator.validate(symbol, response);
            if (!validationResult.isValid()) {
                throw new IllegalStateException("Binance ticker price fallback is invalid: " + validationResult.details());
            }

            return new MarketPriceSnapshot(
                    symbol,
                    new BigDecimal(response.price()),
                    clock.instant()
            );
        } catch (RuntimeException exception) {
            if (lastException != null) {
                exception.addSuppressed(lastException);
            }
            throw new IllegalStateException("Failed to fetch current price from Binance for " + symbol, exception);
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

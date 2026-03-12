package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceTickerPriceResponseValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BinanceMarketClientTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-12T13:46:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private BinanceApiClient binanceApiClient;

    @Test
    void getCurrentPriceReturnsAggregateTradeSnapshotWhenAvailable() {
        BinanceMarketClient client = new BinanceMarketClient(
                binanceApiClient,
                new BinanceTickerPriceResponseValidator(),
                FIXED_CLOCK
        );

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT"))
                .thenReturn(new BinanceAggregateTradeResponse(1L, "83542.12", "0.1", 1L, 1L, 1741787100000L, false, true));

        MarketPriceSnapshot result = client.getCurrentPrice("BTCUSDT");

        assertThat(result.symbol()).isEqualTo("BTCUSDT");
        assertThat(result.price()).isEqualByComparingTo("83542.12");
        assertThat(result.sourceEventTime()).isEqualTo(Instant.ofEpochMilli(1741787100000L));
        verify(binanceApiClient, never()).getTickerPrice("BTCUSDT");
    }

    @Test
    void getCurrentPriceRetriesAggregateTradeBeforeReturningSuccess() {
        BinanceMarketClient client = new BinanceMarketClient(
                binanceApiClient,
                new BinanceTickerPriceResponseValidator(),
                FIXED_CLOCK
        );

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT"))
                .thenThrow(new IllegalStateException("temporary timeout"))
                .thenReturn(new BinanceAggregateTradeResponse(2L, null, "0.2", 2L, 2L, 1741787160000L, false, true))
                .thenReturn(new BinanceAggregateTradeResponse(3L, "83610.55", "0.3", 3L, 3L, 1741787220000L, false, true));

        MarketPriceSnapshot result = client.getCurrentPrice("BTCUSDT");

        assertThat(result.price()).isEqualByComparingTo("83610.55");
        assertThat(result.sourceEventTime()).isEqualTo(Instant.ofEpochMilli(1741787220000L));
        verify(binanceApiClient, times(3)).getLatestAggregateTrade("BTCUSDT");
        verify(binanceApiClient, never()).getTickerPrice("BTCUSDT");
    }

    @Test
    void getCurrentPriceFallsBackToTickerPriceAfterAggregateTradeRetriesFail() {
        BinanceMarketClient client = new BinanceMarketClient(
                binanceApiClient,
                new BinanceTickerPriceResponseValidator(),
                FIXED_CLOCK
        );

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT"))
                .thenThrow(new IllegalStateException("timeout-1"))
                .thenThrow(new IllegalStateException("timeout-2"))
                .thenReturn(null);
        when(binanceApiClient.getTickerPrice("BTCUSDT"))
                .thenReturn(new BinanceTickerPriceResponse("BTCUSDT", "83777.01"));

        MarketPriceSnapshot result = client.getCurrentPrice("BTCUSDT");

        assertThat(result.price()).isEqualByComparingTo("83777.01");
        assertThat(result.sourceEventTime()).isEqualTo(FIXED_CLOCK.instant());
        verify(binanceApiClient, times(3)).getLatestAggregateTrade("BTCUSDT");
        verify(binanceApiClient).getTickerPrice("BTCUSDT");
    }

    @Test
    void getCurrentPriceThrowsWhenAggregateTradeAndTickerFallbackBothFail() {
        BinanceMarketClient client = new BinanceMarketClient(
                binanceApiClient,
                new BinanceTickerPriceResponseValidator(),
                FIXED_CLOCK
        );

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT"))
                .thenThrow(new IllegalStateException("timeout-1"))
                .thenThrow(new IllegalStateException("timeout-2"))
                .thenThrow(new IllegalStateException("timeout-3"));
        when(binanceApiClient.getTickerPrice("BTCUSDT"))
                .thenReturn(new BinanceTickerPriceResponse("ETHUSDT", "2000"));

        assertThatThrownBy(() -> client.getCurrentPrice("BTCUSDT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to fetch current price from Binance for BTCUSDT");

        verify(binanceApiClient, times(3)).getLatestAggregateTrade("BTCUSDT");
        verify(binanceApiClient).getTickerPrice("BTCUSDT");
    }
}

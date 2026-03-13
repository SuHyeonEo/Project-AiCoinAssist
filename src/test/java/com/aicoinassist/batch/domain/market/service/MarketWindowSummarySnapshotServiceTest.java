package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketWindowSummarySnapshotServiceTest {

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Test
    void createIncludesQuoteVolumeTradeCountAndTakerBuyParticipation() {
        MarketWindowSummarySnapshotService service = new MarketWindowSummarySnapshotService(
                marketIndicatorSnapshotRepository,
                marketCandleRawRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = MarketIndicatorSnapshotEntity.builder()
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                .latestCandleOpenTime(Instant.parse("2026-03-10T00:00:00Z"))
                .priceSourceEventTime(Instant.parse("2026-03-10T00:59:30Z"))
                .sourceDataVersion("basis-key")
                .currentPrice(new BigDecimal("87500"))
                .ma20(new BigDecimal("87000"))
                .ma60(new BigDecimal("86000"))
                .ma120(new BigDecimal("85000"))
                .rsi14(new BigDecimal("62"))
                .macdLine(new BigDecimal("120"))
                .macdSignalLine(new BigDecimal("100"))
                .macdHistogram(new BigDecimal("20"))
                .atr14(new BigDecimal("1500"))
                .bollingerUpperBand(new BigDecimal("88500"))
                .bollingerMiddleBand(new BigDecimal("87000"))
                .bollingerLowerBand(new BigDecimal("85500"))
                .build();

        List<Candle> candles = List.of(
                new Candle(
                        Instant.parse("2026-03-09T23:00:00Z"),
                        Instant.parse("2026-03-09T23:59:59Z"),
                        new BigDecimal("86800"),
                        new BigDecimal("87600"),
                        new BigDecimal("86600"),
                        new BigDecimal("87200"),
                        new BigDecimal("100")
                ),
                new Candle(
                        Instant.parse("2026-03-10T00:00:00Z"),
                        Instant.parse("2026-03-10T00:59:59Z"),
                        new BigDecimal("87200"),
                        new BigDecimal("87800"),
                        new BigDecimal("87000"),
                        new BigDecimal("87500"),
                        new BigDecimal("122")
                )
        );
        List<MarketIndicatorSnapshotEntity> indicatorSnapshots = List.of(indicatorSnapshot("1400"), indicatorSnapshot("1500"));
        List<MarketCandleRawEntity> rawCandles = List.of(
                rawCandle(Instant.parse("2026-03-09T23:00:00Z"), "100", "10800000", 12345L, "6120000"),
                rawCandle(Instant.parse("2026-03-10T00:00:00Z"), "122", "12500000", 13500L, "7083333.75")
        );

        when(marketIndicatorSnapshotRepository
                .findAllBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAsc(
                        "BTCUSDT",
                        "1h",
                        Instant.parse("2026-03-03T00:59:59Z"),
                        Instant.parse("2026-03-10T00:59:59Z")
                ))
                .thenReturn(indicatorSnapshots);
        when(marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        "BTCUSDT",
                        "1h",
                        Instant.parse("2026-03-03T00:59:59Z"),
                        Instant.parse("2026-03-10T00:59:59Z"),
                        RawDataValidationStatus.VALID
                ))
                .thenReturn(rawCandles);

        MarketWindowSummarySnapshot result = service.create(currentSnapshot, MarketWindowType.LAST_7D, candles);

        assertThat(result.averageQuoteAssetVolume()).isEqualByComparingTo("11650000.00000000");
        assertThat(result.averageTradeCount()).isEqualByComparingTo("12922.50000000");
        assertThat(result.currentQuoteAssetVolumeVsAverage()).isEqualByComparingTo("0.07296137");
        assertThat(result.currentTradeCountVsAverage()).isEqualByComparingTo("0.04468950");
        assertThat(result.currentTakerBuyQuoteRatio()).isEqualByComparingTo("0.56666670");
    }

    private MarketIndicatorSnapshotEntity indicatorSnapshot(String atr) {
        return MarketIndicatorSnapshotEntity.builder()
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                .latestCandleOpenTime(Instant.parse("2026-03-10T00:00:00Z"))
                .priceSourceEventTime(Instant.parse("2026-03-10T00:59:30Z"))
                .sourceDataVersion("basis-key")
                .currentPrice(new BigDecimal("87500"))
                .ma20(new BigDecimal("87000"))
                .ma60(new BigDecimal("86000"))
                .ma120(new BigDecimal("85000"))
                .rsi14(new BigDecimal("62"))
                .macdLine(new BigDecimal("120"))
                .macdSignalLine(new BigDecimal("100"))
                .macdHistogram(new BigDecimal("20"))
                .atr14(new BigDecimal(atr))
                .bollingerUpperBand(new BigDecimal("88500"))
                .bollingerMiddleBand(new BigDecimal("87000"))
                .bollingerLowerBand(new BigDecimal("85500"))
                .build();
    }

    private MarketCandleRawEntity rawCandle(
            Instant openTime,
            String volume,
            String quoteAssetVolume,
            Long numberOfTrades,
            String takerBuyQuoteAssetVolume
    ) {
        return MarketCandleRawEntity.builder()
                .source("BINANCE")
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .openTime(openTime)
                .closeTime(openTime.plusSeconds(3599))
                .openPrice(new BigDecimal("87000"))
                .highPrice(new BigDecimal("87800"))
                .lowPrice(new BigDecimal("86600"))
                .closePrice(new BigDecimal("87500"))
                .volume(new BigDecimal(volume))
                .quoteAssetVolume(new BigDecimal(quoteAssetVolume))
                .numberOfTrades(numberOfTrades)
                .takerBuyBaseAssetVolume(new BigDecimal("70"))
                .takerBuyQuoteAssetVolume(new BigDecimal(takerBuyQuoteAssetVolume))
                .collectedTime(openTime.plusSeconds(3601))
                .validationStatus(RawDataValidationStatus.VALID)
                .rawPayload("[\"raw\"]")
                .build();
    }
}

package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.calculator.AtrCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.BollingerBandsCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MacdCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MovingAverageCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.RsiCalculator;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketIndicatorSnapshotServiceTest {

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Mock
    private MarketAnalysisPriceService marketAnalysisPriceService;

    @Test
    void createBuildsSnapshotFromClosedRawCandles() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketIndicatorSnapshotService service = new MarketIndicatorSnapshotService(
                marketCandleRawRepository,
                marketAnalysisPriceService,
                new MovingAverageCalculator(),
                new RsiCalculator(),
                new MacdCalculator(),
                new AtrCalculator(),
                new BollingerBandsCalculator(),
                clock
        );

        Instant latestOpenTime = Instant.parse("2026-03-13T09:00:00Z");
        Instant startOpenTime = latestOpenTime.minus(CandleInterval.ONE_HOUR.duration().multipliedBy(119));
        List<MarketCandleRawEntity> rawCandles = IntStream.range(0, 120)
                .mapToObj(index -> rawCandle(startOpenTime.plus(CandleInterval.ONE_HOUR.duration().multipliedBy(index)), index))
                .toList();

        when(marketCandleRawRepository.findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                "BTCUSDT",
                "1h",
                startOpenTime,
                latestOpenTime,
                RawDataValidationStatus.VALID
        )).thenReturn(rawCandles);
        when(marketAnalysisPriceService.getLatestAnalysisPrice("BTCUSDT"))
                .thenReturn(new com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot(
                        "BTCUSDT",
                        new BigDecimal("10150"),
                        Instant.parse("2026-03-13T10:14:30Z")
                ));

        var snapshot = service.create("BTCUSDT", CandleInterval.ONE_HOUR);

        assertThat(snapshot.candles()).hasSize(120);
        assertThat(snapshot.priceSnapshot().price()).isEqualByComparingTo("10150");
        assertThat(snapshot.priceSnapshot().sourceEventTime()).isEqualTo(Instant.parse("2026-03-13T10:14:30Z"));
        assertThat(snapshot.ma20().value()).isNotNull();
        assertThat(snapshot.rsi14().value()).isNotNull();
    }

    @Test
    void createFailsWhenRawCandleCoverageHasGap() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketIndicatorSnapshotService service = new MarketIndicatorSnapshotService(
                marketCandleRawRepository,
                marketAnalysisPriceService,
                new MovingAverageCalculator(),
                new RsiCalculator(),
                new MacdCalculator(),
                new AtrCalculator(),
                new BollingerBandsCalculator(),
                clock
        );

        Instant latestOpenTime = Instant.parse("2026-03-13T09:00:00Z");
        Instant startOpenTime = latestOpenTime.minus(CandleInterval.ONE_HOUR.duration().multipliedBy(119));
        List<MarketCandleRawEntity> rawCandles = IntStream.range(0, 120)
                .filter(index -> index != 60)
                .mapToObj(index -> rawCandle(startOpenTime.plus(CandleInterval.ONE_HOUR.duration().multipliedBy(index)), index))
                .toList();

        when(marketCandleRawRepository.findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                "BTCUSDT",
                "1h",
                startOpenTime,
                latestOpenTime,
                RawDataValidationStatus.VALID
        )).thenReturn(rawCandles);

        assertThatThrownBy(() -> service.create("BTCUSDT", CandleInterval.ONE_HOUR))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient raw candles");
    }

    private MarketCandleRawEntity rawCandle(Instant openTime, int index) {
        BigDecimal closePrice = new BigDecimal(10000 + index);
        return MarketCandleRawEntity.builder()
                .source("BINANCE")
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .openTime(openTime)
                .closeTime(openTime.plus(CandleInterval.ONE_HOUR.duration()))
                .openPrice(closePrice.subtract(BigDecimal.ONE))
                .highPrice(closePrice.add(BigDecimal.ONE))
                .lowPrice(closePrice.subtract(new BigDecimal("2")))
                .closePrice(closePrice)
                .volume(new BigDecimal("100").add(BigDecimal.valueOf(index)))
                .collectedTime(openTime.plus(CandleInterval.ONE_HOUR.duration()).plusSeconds(5))
                .validationStatus(RawDataValidationStatus.VALID)
                .rawPayload("[\"raw\"]")
                .build();
    }
}

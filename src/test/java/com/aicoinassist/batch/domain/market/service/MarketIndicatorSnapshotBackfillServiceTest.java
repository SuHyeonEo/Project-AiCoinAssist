package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.calculator.AtrCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.BollingerBandsCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MacdCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MovingAverageCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.RsiCalculator;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketIndicatorSnapshotBackfillServiceTest {

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Test
    void rebuildFromRawCreatesIndicatorSnapshotFromValidContiguousCandles() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketIndicatorSnapshotBackfillService service = new MarketIndicatorSnapshotBackfillService(
                marketCandleRawRepository,
                marketIndicatorSnapshotRepository,
                new MovingAverageCalculator(),
                new RsiCalculator(),
                new MacdCalculator(),
                new AtrCalculator(),
                new BollingerBandsCalculator(),
                clock
        );

        Instant expectedLatestOpenTime = Instant.parse("2026-03-13T09:00:00Z");
        Instant expectedStartOpenTime = expectedLatestOpenTime.minus(CandleInterval.ONE_HOUR.duration().multipliedBy(119));
        List<MarketCandleRawEntity> rawCandles = IntStream.range(0, 120)
                .mapToObj(index -> candle(expectedStartOpenTime.plus(CandleInterval.ONE_HOUR.duration().multipliedBy(index)), index))
                .toList();

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "1h"))
                .thenReturn(Optional.empty());
        when(marketCandleRawRepository.findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                "BTCUSDT",
                "1h",
                expectedLatestOpenTime.minus(CandleInterval.ONE_HOUR.duration().multipliedBy(719)),
                expectedLatestOpenTime,
                RawDataValidationStatus.VALID
        )).thenReturn(rawCandles);
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(marketIndicatorSnapshotRepository.save(any(MarketIndicatorSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int rebuilt = service.rebuildFromRaw("BTCUSDT", CandleInterval.ONE_HOUR);

        ArgumentCaptor<MarketIndicatorSnapshotEntity> captor = ArgumentCaptor.forClass(MarketIndicatorSnapshotEntity.class);
        verify(marketIndicatorSnapshotRepository).save(captor.capture());
        assertThat(rebuilt).isEqualTo(1);
        assertThat(captor.getValue().getSnapshotTime()).isEqualTo(expectedLatestOpenTime.plus(CandleInterval.ONE_HOUR.duration()));
        assertThat(captor.getValue().getCurrentPrice()).isEqualByComparingTo("10119");
        assertThat(captor.getValue().getSourceDataVersion()).contains("source=market_candle_raw");
    }

    private MarketCandleRawEntity candle(Instant openTime, int index) {
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

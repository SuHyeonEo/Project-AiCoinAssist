package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.dto.AtrResult;
import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.indicator.dto.MacdResult;
import com.aicoinassist.batch.domain.indicator.dto.MovingAverageResult;
import com.aicoinassist.batch.domain.indicator.dto.RsiResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketIndicatorSnapshotPersistenceServiceTest {

    @Mock
    private MarketIndicatorSnapshotService marketIndicatorSnapshotService;

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshotWhenKeyMatches() {
        MarketIndicatorSnapshotPersistenceService service = new MarketIndicatorSnapshotPersistenceService(
                marketIndicatorSnapshotService,
                marketIndicatorSnapshotRepository
        );

        Instant snapshotTime = Instant.parse("2026-03-09T00:59:59Z");
        MarketIndicatorSnapshot snapshot = snapshot(snapshotTime, "87500.12", "20");
        MarketIndicatorSnapshotEntity existingEntity = MarketIndicatorSnapshotEntity.builder()
                                                                                    .symbol("BTCUSDT")
                                                                                    .intervalValue("1h")
                                                                                    .snapshotTime(snapshotTime)
                                                                                    .currentPrice(new BigDecimal("87000"))
                                                                                    .ma20(new BigDecimal("10"))
                                                                                    .ma60(new BigDecimal("11"))
                                                                                    .ma120(new BigDecimal("12"))
                                                                                    .rsi14(new BigDecimal("40"))
                                                                                    .macdLine(new BigDecimal("1"))
                                                                                    .macdSignalLine(new BigDecimal("0.8"))
                                                                                    .macdHistogram(new BigDecimal("0.2"))
                                                                                    .atr14(new BigDecimal("2"))
                                                                                    .bollingerUpperBand(new BigDecimal("15"))
                                                                                    .bollingerMiddleBand(new BigDecimal("10"))
                                                                                    .bollingerLowerBand(new BigDecimal("5"))
                                                                                    .build();

        when(marketIndicatorSnapshotService.create("BTCUSDT", CandleInterval.ONE_HOUR)).thenReturn(snapshot);
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshotTime
        )).thenReturn(Optional.of(existingEntity));

        MarketIndicatorSnapshotEntity result = service.createAndSave("BTCUSDT", CandleInterval.ONE_HOUR);

        verify(marketIndicatorSnapshotRepository, never()).save(any(MarketIndicatorSnapshotEntity.class));

        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getCurrentPrice()).isEqualByComparingTo("87500.12");
        assertThat(existingEntity.getMa20()).isEqualByComparingTo("20");
        assertThat(existingEntity.getRsi14()).isEqualByComparingTo("55");
        assertThat(existingEntity.getBollingerUpperBand()).isEqualByComparingTo("25");
    }

    @Test
    void createAndSavePersistsNewSnapshotWhenKeyDoesNotExist() {
        MarketIndicatorSnapshotPersistenceService service = new MarketIndicatorSnapshotPersistenceService(
                marketIndicatorSnapshotService,
                marketIndicatorSnapshotRepository
        );

        Instant snapshotTime = Instant.parse("2026-03-09T00:59:59Z");
        MarketIndicatorSnapshot snapshot = snapshot(snapshotTime, "87500.12", "20");

        when(marketIndicatorSnapshotService.create("BTCUSDT", CandleInterval.ONE_HOUR)).thenReturn(snapshot);
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshotTime
        )).thenReturn(Optional.empty());
        when(marketIndicatorSnapshotRepository.save(any(MarketIndicatorSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketIndicatorSnapshotEntity result = service.createAndSave("BTCUSDT", CandleInterval.ONE_HOUR);

        assertThat(result.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(result.getIntervalValue()).isEqualTo("1h");
        assertThat(result.getSnapshotTime()).isEqualTo(snapshotTime);
        assertThat(result.getCurrentPrice()).isEqualByComparingTo("87500.12");
        assertThat(result.getMa20()).isEqualByComparingTo("20");
    }

    private MarketIndicatorSnapshot snapshot(
            Instant snapshotTime,
            String currentPrice,
            String ma20
    ) {
        Candle latestCandle = new Candle(
                snapshotTime.minusSeconds(3600),
                snapshotTime,
                new BigDecimal("10"),
                new BigDecimal("12"),
                new BigDecimal("9"),
                new BigDecimal("11"),
                new BigDecimal("100")
        );

        return new MarketIndicatorSnapshot(
                "BTCUSDT",
                new MarketPriceSnapshot("BTCUSDT", new BigDecimal(currentPrice)),
                List.of(latestCandle),
                new MovingAverageResult(20, new BigDecimal(ma20)),
                new MovingAverageResult(60, new BigDecimal("30")),
                new MovingAverageResult(120, new BigDecimal("40")),
                new RsiResult(14, new BigDecimal("55")),
                new MacdResult(new BigDecimal("1.2"), new BigDecimal("0.7"), new BigDecimal("0.5")),
                new AtrResult(14, new BigDecimal("2.5")),
                new BollingerBandsResult(20, new BigDecimal("25"), new BigDecimal("20"), new BigDecimal("15"))
        );
    }
}

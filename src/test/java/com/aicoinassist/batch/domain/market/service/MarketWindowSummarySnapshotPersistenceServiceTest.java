package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketWindowSummarySnapshotRepository;
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
class MarketWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private MarketWindowSummarySnapshotService marketWindowSummarySnapshotService;

    @Mock
    private MarketWindowSummarySnapshotRepository marketWindowSummarySnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSummaryWhenKeyMatches() {
        MarketWindowSummarySnapshotPersistenceService service = new MarketWindowSummarySnapshotPersistenceService(
                marketWindowSummarySnapshotService,
                marketWindowSummarySnapshotRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = currentSnapshot();
        List<Candle> candles = List.of();
        MarketWindowSummarySnapshot summary = summary();
        MarketWindowSummarySnapshotEntity existingEntity = MarketWindowSummarySnapshotEntity.builder()
                                                                                            .symbol("BTCUSDT")
                                                                                            .intervalValue("1h")
                                                                                            .windowType(MarketWindowType.LAST_7D.name())
                                                                                            .windowStartTime(summary.windowStartTime().minusSeconds(3600))
                                                                                            .windowEndTime(summary.windowEndTime())
                                                                                            .sampleCount(100)
                                                                                            .currentPrice(new BigDecimal("87000"))
                                                                                            .windowHigh(new BigDecimal("90000"))
                                                                                            .windowLow(new BigDecimal("82000"))
                                                                                            .windowRange(new BigDecimal("8000"))
                                                                                            .currentPositionInRange(new BigDecimal("0.5"))
                                                                                            .distanceFromWindowHigh(new BigDecimal("0.1"))
                                                                                            .reboundFromWindowLow(new BigDecimal("0.06"))
                                                                                            .averageVolume(new BigDecimal("100"))
                                                                                            .averageAtr(new BigDecimal("1200"))
                                                                                            .currentVolume(new BigDecimal("110"))
                                                                                            .currentAtr(new BigDecimal("1500"))
                                                                                            .currentVolumeVsAverage(new BigDecimal("0.1"))
                                                                                            .currentAtrVsAverage(new BigDecimal("0.25"))
                                                                                            .sourceDataVersion("old")
                                                                                            .build();

        when(marketWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_7D, candles)).thenReturn(summary);
        when(marketWindowSummarySnapshotRepository.findTopBySymbolAndIntervalValueAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                "LAST_7D",
                summary.windowEndTime()
        )).thenReturn(Optional.of(existingEntity));

        MarketWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_7D, candles);

        verify(marketWindowSummarySnapshotRepository, never()).save(any(MarketWindowSummarySnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getSampleCount()).isEqualTo(168);
        assertThat(existingEntity.getCurrentPositionInRange()).isEqualByComparingTo("0.68750000");
        assertThat(existingEntity.getSourceDataVersion()).isEqualTo(summary.sourceDataVersion());
    }

    @Test
    void createAndSavePersistsNewSummaryWhenKeyDoesNotExist() {
        MarketWindowSummarySnapshotPersistenceService service = new MarketWindowSummarySnapshotPersistenceService(
                marketWindowSummarySnapshotService,
                marketWindowSummarySnapshotRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = currentSnapshot();
        List<Candle> candles = List.of();
        MarketWindowSummarySnapshot summary = summary();

        when(marketWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_7D, candles)).thenReturn(summary);
        when(marketWindowSummarySnapshotRepository.findTopBySymbolAndIntervalValueAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                "LAST_7D",
                summary.windowEndTime()
        )).thenReturn(Optional.empty());
        when(marketWindowSummarySnapshotRepository.save(any(MarketWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_7D, candles);

        assertThat(result.getWindowType()).isEqualTo("LAST_7D");
        assertThat(result.getAverageAtr()).isEqualByComparingTo("1450.00000000");
        assertThat(result.getCurrentVolumeVsAverage()).isEqualByComparingTo("0.22000000");
    }

    private MarketIndicatorSnapshotEntity currentSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-09T23:59:59Z"))
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
    }

    private MarketWindowSummarySnapshot summary() {
        return new MarketWindowSummarySnapshot(
                "BTCUSDT",
                "1h",
                MarketWindowType.LAST_7D,
                Instant.parse("2026-03-03T00:59:59Z"),
                Instant.parse("2026-03-10T00:59:59Z"),
                168,
                new BigDecimal("87500"),
                new BigDecimal("91000"),
                new BigDecimal("83000"),
                new BigDecimal("8000"),
                new BigDecimal("0.68750000"),
                new BigDecimal("0.03846154"),
                new BigDecimal("0.05421687"),
                new BigDecimal("100.00000000"),
                new BigDecimal("1450.00000000"),
                new BigDecimal("122.00000000"),
                new BigDecimal("1500.00000000"),
                new BigDecimal("0.22000000"),
                new BigDecimal("0.03448276"),
                "basis-key;windowType=LAST_7D;windowStartTime=2026-03-03T00:59:59Z;windowEndTime=2026-03-10T00:59:59Z;sampleCount=168"
        );
    }
}

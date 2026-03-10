package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.repository.MarketCandidateLevelSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class MarketCandidateLevelSnapshotPersistenceServiceTest {

    @Mock
    private MarketCandidateLevelSnapshotService marketCandidateLevelSnapshotService;

    @Mock
    private MarketCandidateLevelSnapshotRepository marketCandidateLevelSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingCandidateLevelWhenKeyMatches() {
        MarketCandidateLevelSnapshotPersistenceService service = new MarketCandidateLevelSnapshotPersistenceService(
                marketCandidateLevelSnapshotService,
                marketCandidateLevelSnapshotRepository,
                new ObjectMapper()
        );

        MarketIndicatorSnapshotEntity currentSnapshot = currentSnapshot();
        MarketCandidateLevelSnapshot snapshot = snapshot();
        MarketCandidateLevelSnapshotEntity existingEntity = MarketCandidateLevelSnapshotEntity.builder()
                                                                                              .symbol("BTCUSDT")
                                                                                              .intervalValue("1h")
                                                                                              .snapshotTime(snapshot.snapshotTime())
                                                                                              .referenceTime(snapshot.referenceTime())
                                                                                              .levelType(MarketCandidateLevelType.SUPPORT.name())
                                                                                              .levelLabel(MarketCandidateLevelLabel.MA20.name())
                                                                                              .sourceType(MarketCandidateLevelSourceType.MOVING_AVERAGE.name())
                                                                                              .currentPrice(new BigDecimal("87000"))
                                                                                              .levelPrice(new BigDecimal("86500"))
                                                                                              .distanceFromCurrent(new BigDecimal("0.00500000"))
                                                                                              .strengthScore(new BigDecimal("0.62000000"))
                                                                                              .reactionCount(1)
                                                                                              .clusterSize(1)
                                                                                              .rationale("Old support")
                                                                                              .triggerFactsPayload("[\"old\"]")
                                                                                              .sourceDataVersion("old")
                                                                                              .build();

        when(marketCandidateLevelSnapshotService.createAll(currentSnapshot)).thenReturn(List.of(snapshot));
        when(marketCandidateLevelSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeAndLevelTypeAndLevelLabelOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime(),
                "SUPPORT",
                "MA20"
        )).thenReturn(Optional.of(existingEntity));

        List<MarketCandidateLevelSnapshotEntity> result = service.createAndSaveAll(currentSnapshot);

        verify(marketCandidateLevelSnapshotRepository, never()).save(any(MarketCandidateLevelSnapshotEntity.class));
        assertThat(result).containsExactly(existingEntity);
        assertThat(existingEntity.getLevelPrice()).isEqualByComparingTo("87000.00000000");
        assertThat(existingEntity.getStrengthScore()).isEqualByComparingTo("0.64428571");
        assertThat(existingEntity.getReactionCount()).isEqualTo(2);
        assertThat(existingEntity.getTriggerFactsPayload()).contains("MA20");
    }

    @Test
    void createAndSavePersistsNewCandidateLevelWhenKeyDoesNotExist() {
        MarketCandidateLevelSnapshotPersistenceService service = new MarketCandidateLevelSnapshotPersistenceService(
                marketCandidateLevelSnapshotService,
                marketCandidateLevelSnapshotRepository,
                new ObjectMapper()
        );

        MarketIndicatorSnapshotEntity currentSnapshot = currentSnapshot();
        MarketCandidateLevelSnapshot snapshot = snapshot();

        when(marketCandidateLevelSnapshotService.createAll(currentSnapshot)).thenReturn(List.of(snapshot));
        when(marketCandidateLevelSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeAndLevelTypeAndLevelLabelOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime(),
                "SUPPORT",
                "MA20"
        )).thenReturn(Optional.empty());
        when(marketCandidateLevelSnapshotRepository.save(any(MarketCandidateLevelSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MarketCandidateLevelSnapshotEntity> result = service.createAndSaveAll(currentSnapshot);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevelLabel()).isEqualTo("MA20");
        assertThat(result.get(0).getDistanceFromCurrent()).isEqualByComparingTo("0.00571429");
        assertThat(result.get(0).getTriggerFactsPayload()).contains("SUPPORT distance 0.57%");
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

    private MarketCandidateLevelSnapshot snapshot() {
        return new MarketCandidateLevelSnapshot(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-10T00:59:59Z"),
                Instant.parse("2026-03-10T00:59:59Z"),
                MarketCandidateLevelType.SUPPORT,
                MarketCandidateLevelLabel.MA20,
                MarketCandidateLevelSourceType.MOVING_AVERAGE,
                new BigDecimal("87500.00000000"),
                new BigDecimal("87000.00000000"),
                new BigDecimal("0.00571429"),
                new BigDecimal("0.64428571"),
                2,
                1,
                "Short-term average support",
                List.of("Current price 87500 vs MA20 87000", "SUPPORT distance 0.57%"),
                "basis-key;levelType=SUPPORT;levelLabel=MA20"
        );
    }
}

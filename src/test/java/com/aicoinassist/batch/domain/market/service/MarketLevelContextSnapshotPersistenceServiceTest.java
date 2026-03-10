package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketLevelContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;
import com.aicoinassist.batch.domain.market.repository.MarketLevelContextSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketLevelContextSnapshotPersistenceServiceTest {

    @Mock
    private MarketLevelContextSnapshotService marketLevelContextSnapshotService;

    @Mock
    private MarketLevelContextSnapshotRepository marketLevelContextSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshotWhenIdentityMatches() {
        MarketLevelContextSnapshotPersistenceService service = new MarketLevelContextSnapshotPersistenceService(
                marketLevelContextSnapshotService,
                marketLevelContextSnapshotRepository
        );

        MarketIndicatorSnapshotEntity indicatorSnapshot = indicatorSnapshot();
        List<MarketCandidateLevelZoneSnapshotEntity> zones = List.of();
        MarketLevelContextSnapshot snapshot = snapshot();
        MarketLevelContextSnapshotEntity existingEntity = MarketLevelContextSnapshotEntity.builder()
                                                                                          .symbol("BTCUSDT")
                                                                                          .intervalValue("1h")
                                                                                          .snapshotTime(snapshot.snapshotTime())
                                                                                          .currentPrice(new BigDecimal("87000.00000000"))
                                                                                          .supportZoneRank(1)
                                                                                          .supportRepresentativePrice(new BigDecimal("86500.00000000"))
                                                                                          .supportZoneLow(new BigDecimal("86000.00000000"))
                                                                                          .supportZoneHigh(new BigDecimal("86800.00000000"))
                                                                                          .supportDistanceToZone(new BigDecimal("0.00700000"))
                                                                                          .supportZoneStrength(new BigDecimal("0.70000000"))
                                                                                          .supportInteractionType(MarketCandidateLevelZoneInteractionType.ABOVE_ZONE.name())
                                                                                          .supportRecentTestCount(3)
                                                                                          .supportRecentRejectionCount(2)
                                                                                          .supportRecentBreakCount(1)
                                                                                          .supportBreakRisk(new BigDecimal("0.12000000"))
                                                                                          .resistanceZoneRank(1)
                                                                                          .resistanceRepresentativePrice(new BigDecimal("88500.00000000"))
                                                                                          .resistanceZoneLow(new BigDecimal("88400.00000000"))
                                                                                          .resistanceZoneHigh(new BigDecimal("88600.00000000"))
                                                                                          .resistanceDistanceToZone(new BigDecimal("0.01000000"))
                                                                                          .resistanceZoneStrength(new BigDecimal("0.76000000"))
                                                                                          .resistanceInteractionType(MarketCandidateLevelZoneInteractionType.BELOW_ZONE.name())
                                                                                          .resistanceRecentTestCount(3)
                                                                                          .resistanceRecentRejectionCount(2)
                                                                                          .resistanceRecentBreakCount(0)
                                                                                          .resistanceBreakRisk(new BigDecimal("0.08000000"))
                                                                                          .sourceDataVersion("old")
                                                                                          .build();

        when(marketLevelContextSnapshotService.create(indicatorSnapshot, zones)).thenReturn(snapshot);
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime()
        )).thenReturn(Optional.of(existingEntity));

        MarketLevelContextSnapshotEntity result = service.createAndSave(indicatorSnapshot, zones);

        verify(marketLevelContextSnapshotRepository, never()).save(existingEntity);
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getCurrentPrice()).isEqualByComparingTo("87500.00000000");
        assertThat(existingEntity.getSupportBreakRisk()).isEqualByComparingTo("0.18000000");
        assertThat(existingEntity.getResistanceZoneHigh()).isEqualByComparingTo("88800.00000000");
    }

    @Test
    void createAndSavePersistsNewSnapshotWhenIdentityDoesNotExist() {
        MarketLevelContextSnapshotPersistenceService service = new MarketLevelContextSnapshotPersistenceService(
                marketLevelContextSnapshotService,
                marketLevelContextSnapshotRepository
        );

        MarketIndicatorSnapshotEntity indicatorSnapshot = indicatorSnapshot();
        List<MarketCandidateLevelZoneSnapshotEntity> zones = List.of();
        MarketLevelContextSnapshot snapshot = snapshot();

        when(marketLevelContextSnapshotService.create(indicatorSnapshot, zones)).thenReturn(snapshot);
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime()
        )).thenReturn(Optional.empty());
        when(marketLevelContextSnapshotRepository.save(org.mockito.ArgumentMatchers.any(MarketLevelContextSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketLevelContextSnapshotEntity result = service.createAndSave(indicatorSnapshot, zones);

        assertThat(result.getSupportZoneRank()).isEqualTo(1);
        assertThat(result.getResistanceZoneRank()).isEqualTo(1);
        assertThat(result.getSourceDataVersion()).contains("indicator=basis-key");
    }

    private MarketIndicatorSnapshotEntity indicatorSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-09T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-10T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("87500.00000000"))
                                            .ma20(new BigDecimal("87000.00000000"))
                                            .ma60(new BigDecimal("86000.00000000"))
                                            .ma120(new BigDecimal("85000.00000000"))
                                            .rsi14(new BigDecimal("60.00000000"))
                                            .macdLine(new BigDecimal("20.00000000"))
                                            .macdSignalLine(new BigDecimal("18.00000000"))
                                            .macdHistogram(new BigDecimal("2.00000000"))
                                            .atr14(new BigDecimal("1500.00000000"))
                                            .bollingerUpperBand(new BigDecimal("88500.00000000"))
                                            .bollingerMiddleBand(new BigDecimal("87000.00000000"))
                                            .bollingerLowerBand(new BigDecimal("85500.00000000"))
                                            .build();
    }

    private MarketLevelContextSnapshot snapshot() {
        return new MarketLevelContextSnapshot(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-10T00:59:59Z"),
                new BigDecimal("87500.00000000"),
                1,
                new BigDecimal("86600.00000000"),
                new BigDecimal("86200.00000000"),
                new BigDecimal("86800.00000000"),
                new BigDecimal("0.00400000"),
                new BigDecimal("0.88000000"),
                MarketCandidateLevelZoneInteractionType.ABOVE_ZONE,
                5,
                4,
                1,
                new BigDecimal("0.18000000"),
                1,
                new BigDecimal("88600.00000000"),
                new BigDecimal("88400.00000000"),
                new BigDecimal("88800.00000000"),
                new BigDecimal("0.01000000"),
                new BigDecimal("0.82000000"),
                MarketCandidateLevelZoneInteractionType.BELOW_ZONE,
                3,
                2,
                0,
                new BigDecimal("0.05000000"),
                "indicator=basis-key;supportZone=support-v1;resistanceZone=resistance-v1"
        );
    }
}

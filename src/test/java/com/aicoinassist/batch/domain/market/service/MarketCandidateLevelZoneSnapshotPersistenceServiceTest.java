package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelZoneSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.repository.MarketCandidateLevelZoneSnapshotRepository;
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
class MarketCandidateLevelZoneSnapshotPersistenceServiceTest {

    @Mock
    private MarketCandidateLevelZoneSnapshotService marketCandidateLevelZoneSnapshotService;

    @Mock
    private MarketCandidateLevelZoneSnapshotRepository marketCandidateLevelZoneSnapshotRepository;

    @Test
    void createAndSaveAllRefreshesExistingZoneWhenIdentityMatches() {
        MarketCandidateLevelZoneSnapshotPersistenceService service = new MarketCandidateLevelZoneSnapshotPersistenceService(
                marketCandidateLevelZoneSnapshotService,
                marketCandidateLevelZoneSnapshotRepository,
                new ObjectMapper()
        );

        List<MarketCandidateLevelSnapshotEntity> levelEntities = List.of();
        MarketCandidateLevelZoneSnapshot snapshot = snapshot();
        MarketCandidateLevelZoneSnapshotEntity existingEntity = MarketCandidateLevelZoneSnapshotEntity.builder()
                                                                                                      .symbol("BTCUSDT")
                                                                                                      .intervalValue("1h")
                                                                                                      .snapshotTime(snapshot.snapshotTime())
                                                                                                      .zoneType(MarketCandidateLevelType.SUPPORT.name())
                                                                                                      .zoneRank(1)
                                                                                                      .currentPrice(new BigDecimal("87500.00000000"))
                                                                                                      .representativePrice(new BigDecimal("86900.00000000"))
                                                                                                      .zoneLow(new BigDecimal("86800.00000000"))
                                                                                                      .zoneHigh(new BigDecimal("87000.00000000"))
                                                                                                      .distanceFromCurrent(new BigDecimal("0.00700000"))
                                                                                                      .zoneStrengthScore(new BigDecimal("0.74000000"))
                                                                                                      .strongestLevelLabel(MarketCandidateLevelLabel.MA20.name())
                                                                                                      .strongestSourceType(MarketCandidateLevelSourceType.MOVING_AVERAGE.name())
                                                                                                      .levelCount(2)
                                                                                                      .includedLevelLabelsPayload("[\"MA20\",\"PIVOT_LOW\"]")
                                                                                                      .includedSourceTypesPayload("[\"MOVING_AVERAGE\",\"PIVOT_LEVEL\"]")
                                                                                                      .triggerFactsPayload("[\"old\"]")
                                                                                                      .sourceDataVersion("old")
                                                                                                      .build();

        when(marketCandidateLevelZoneSnapshotService.createAll(levelEntities)).thenReturn(List.of(snapshot));
        when(marketCandidateLevelZoneSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeAndZoneTypeAndZoneRankOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime(),
                "SUPPORT",
                1
        )).thenReturn(Optional.of(existingEntity));

        List<MarketCandidateLevelZoneSnapshotEntity> result = service.createAndSaveAll(levelEntities);

        verify(marketCandidateLevelZoneSnapshotRepository, never()).save(any(MarketCandidateLevelZoneSnapshotEntity.class));
        assertThat(result).containsExactly(existingEntity);
        assertThat(existingEntity.getRepresentativePrice()).isEqualByComparingTo("86850.00000000");
        assertThat(existingEntity.getZoneStrengthScore()).isEqualByComparingTo("0.89285714");
        assertThat(existingEntity.getTriggerFactsPayload()).contains("SUPPORT zone spans");
    }

    @Test
    void createAndSaveAllPersistsNewZoneWhenIdentityDoesNotExist() {
        MarketCandidateLevelZoneSnapshotPersistenceService service = new MarketCandidateLevelZoneSnapshotPersistenceService(
                marketCandidateLevelZoneSnapshotService,
                marketCandidateLevelZoneSnapshotRepository,
                new ObjectMapper()
        );

        List<MarketCandidateLevelSnapshotEntity> levelEntities = List.of();
        MarketCandidateLevelZoneSnapshot snapshot = snapshot();

        when(marketCandidateLevelZoneSnapshotService.createAll(levelEntities)).thenReturn(List.of(snapshot));
        when(marketCandidateLevelZoneSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeAndZoneTypeAndZoneRankOrderByIdDesc(
                "BTCUSDT",
                "1h",
                snapshot.snapshotTime(),
                "SUPPORT",
                1
        )).thenReturn(Optional.empty());
        when(marketCandidateLevelZoneSnapshotRepository.save(any(MarketCandidateLevelZoneSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MarketCandidateLevelZoneSnapshotEntity> result = service.createAndSaveAll(levelEntities);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getZoneRank()).isEqualTo(1);
        assertThat(result.get(0).getStrongestLevelLabel()).isEqualTo("PIVOT_LOW");
        assertThat(result.get(0).getIncludedLevelLabelsPayload()).contains("MA20");
    }

    private MarketCandidateLevelZoneSnapshot snapshot() {
        return new MarketCandidateLevelZoneSnapshot(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-10T00:59:59Z"),
                MarketCandidateLevelType.SUPPORT,
                1,
                new BigDecimal("87500.00000000"),
                new BigDecimal("86850.00000000"),
                new BigDecimal("86850.00000000"),
                new BigDecimal("87000.00000000"),
                new BigDecimal("0.00742857"),
                new BigDecimal("0.89285714"),
                MarketCandidateLevelLabel.PIVOT_LOW,
                MarketCandidateLevelSourceType.PIVOT_LEVEL,
                2,
                List.of(MarketCandidateLevelLabel.MA20, MarketCandidateLevelLabel.PIVOT_LOW),
                List.of(MarketCandidateLevelSourceType.MOVING_AVERAGE, MarketCandidateLevelSourceType.PIVOT_LEVEL),
                List.of("SUPPORT zone spans 86850 to 87000 with 2 candidate levels."),
                "basis-key;zoneType=SUPPORT;zoneRank=1"
        );
    }
}

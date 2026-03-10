package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketLevelContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketLevelContextSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketLevelContextSnapshotPersistenceService {

    private final MarketLevelContextSnapshotService marketLevelContextSnapshotService;
    private final MarketLevelContextSnapshotRepository marketLevelContextSnapshotRepository;

    @Transactional
    public MarketLevelContextSnapshotEntity createAndSave(
            MarketIndicatorSnapshotEntity snapshot,
            List<MarketCandidateLevelZoneSnapshotEntity> zoneEntities
    ) {
        MarketLevelContextSnapshot levelContextSnapshot = marketLevelContextSnapshotService.create(snapshot, zoneEntities);
        MarketLevelContextSnapshotEntity existingEntity = marketLevelContextSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                        levelContextSnapshot.symbol(),
                        levelContextSnapshot.intervalValue(),
                        levelContextSnapshot.snapshotTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            MarketLevelContextSnapshotEntity entity = MarketLevelContextSnapshotEntity.builder()
                                                                                      .symbol(levelContextSnapshot.symbol())
                                                                                      .intervalValue(levelContextSnapshot.intervalValue())
                                                                                      .snapshotTime(levelContextSnapshot.snapshotTime())
                                                                                      .currentPrice(levelContextSnapshot.currentPrice())
                                                                                      .supportZoneRank(levelContextSnapshot.supportZoneRank())
                                                                                      .supportRepresentativePrice(levelContextSnapshot.supportRepresentativePrice())
                                                                                      .supportZoneLow(levelContextSnapshot.supportZoneLow())
                                                                                      .supportZoneHigh(levelContextSnapshot.supportZoneHigh())
                                                                                      .supportDistanceToZone(levelContextSnapshot.supportDistanceToZone())
                                                                                      .supportZoneStrength(levelContextSnapshot.supportZoneStrength())
                                                                                      .supportInteractionType(enumName(levelContextSnapshot.supportInteractionType()))
                                                                                      .supportRecentTestCount(levelContextSnapshot.supportRecentTestCount())
                                                                                      .supportRecentRejectionCount(levelContextSnapshot.supportRecentRejectionCount())
                                                                                      .supportRecentBreakCount(levelContextSnapshot.supportRecentBreakCount())
                                                                                      .supportBreakRisk(levelContextSnapshot.supportBreakRisk())
                                                                                      .resistanceZoneRank(levelContextSnapshot.resistanceZoneRank())
                                                                                      .resistanceRepresentativePrice(levelContextSnapshot.resistanceRepresentativePrice())
                                                                                      .resistanceZoneLow(levelContextSnapshot.resistanceZoneLow())
                                                                                      .resistanceZoneHigh(levelContextSnapshot.resistanceZoneHigh())
                                                                                      .resistanceDistanceToZone(levelContextSnapshot.resistanceDistanceToZone())
                                                                                      .resistanceZoneStrength(levelContextSnapshot.resistanceZoneStrength())
                                                                                      .resistanceInteractionType(enumName(levelContextSnapshot.resistanceInteractionType()))
                                                                                      .resistanceRecentTestCount(levelContextSnapshot.resistanceRecentTestCount())
                                                                                      .resistanceRecentRejectionCount(levelContextSnapshot.resistanceRecentRejectionCount())
                                                                                      .resistanceRecentBreakCount(levelContextSnapshot.resistanceRecentBreakCount())
                                                                                      .resistanceBreakRisk(levelContextSnapshot.resistanceBreakRisk())
                                                                                      .sourceDataVersion(levelContextSnapshot.sourceDataVersion())
                                                                                      .build();
            return marketLevelContextSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                levelContextSnapshot.currentPrice(),
                levelContextSnapshot.supportZoneRank(),
                levelContextSnapshot.supportRepresentativePrice(),
                levelContextSnapshot.supportZoneLow(),
                levelContextSnapshot.supportZoneHigh(),
                levelContextSnapshot.supportDistanceToZone(),
                levelContextSnapshot.supportZoneStrength(),
                enumName(levelContextSnapshot.supportInteractionType()),
                levelContextSnapshot.supportRecentTestCount(),
                levelContextSnapshot.supportRecentRejectionCount(),
                levelContextSnapshot.supportRecentBreakCount(),
                levelContextSnapshot.supportBreakRisk(),
                levelContextSnapshot.resistanceZoneRank(),
                levelContextSnapshot.resistanceRepresentativePrice(),
                levelContextSnapshot.resistanceZoneLow(),
                levelContextSnapshot.resistanceZoneHigh(),
                levelContextSnapshot.resistanceDistanceToZone(),
                levelContextSnapshot.resistanceZoneStrength(),
                enumName(levelContextSnapshot.resistanceInteractionType()),
                levelContextSnapshot.resistanceRecentTestCount(),
                levelContextSnapshot.resistanceRecentRejectionCount(),
                levelContextSnapshot.resistanceRecentBreakCount(),
                levelContextSnapshot.resistanceBreakRisk(),
                levelContextSnapshot.sourceDataVersion()
        );
        return existingEntity;
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}

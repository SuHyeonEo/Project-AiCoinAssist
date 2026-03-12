package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelZoneSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketCandidateLevelZoneSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketCandidateLevelZoneSnapshotPersistenceService {

    private final MarketCandidateLevelZoneSnapshotService marketCandidateLevelZoneSnapshotService;
    private final MarketCandidateLevelZoneSnapshotRepository marketCandidateLevelZoneSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<MarketCandidateLevelZoneSnapshotEntity> createAndSaveAll(
            List<MarketCandidateLevelSnapshotEntity> levelEntities,
            List<Candle> candles
    ) {
        List<MarketCandidateLevelZoneSnapshotEntity> entities = new ArrayList<>();
        for (MarketCandidateLevelZoneSnapshot snapshot : marketCandidateLevelZoneSnapshotService.createAll(levelEntities, candles)) {
            entities.add(createAndSave(snapshot));
        }
        return entities;
    }

    @Transactional
    public MarketCandidateLevelZoneSnapshotEntity createAndSave(MarketCandidateLevelZoneSnapshot snapshot) {
        MarketCandidateLevelZoneSnapshotEntity existingEntity = marketCandidateLevelZoneSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeAndZoneTypeAndZoneRankOrderByIdDesc(
                        snapshot.symbol(),
                        snapshot.intervalValue(),
                        snapshot.snapshotTime(),
                        snapshot.zoneType().name(),
                        snapshot.zoneRank()
                )
                .orElse(null);

        String includedLevelLabelsPayload = writePayload(snapshot.includedLevelLabels().stream().map(Enum::name).toList());
        String includedSourceTypesPayload = writePayload(snapshot.includedSourceTypes().stream().map(Enum::name).toList());
        String triggerFactsPayload = writePayload(snapshot.triggerFacts());
        if (existingEntity == null) {
            MarketCandidateLevelZoneSnapshotEntity entity = MarketCandidateLevelZoneSnapshotEntity.builder()
                                                                                                  .symbol(snapshot.symbol())
                                                                                                  .intervalValue(snapshot.intervalValue())
                                                                                                  .snapshotTime(snapshot.snapshotTime())
                                                                                                  .zoneType(snapshot.zoneType().name())
                                                                                                  .zoneRank(snapshot.zoneRank())
                                                                                                  .currentPrice(snapshot.currentPrice())
                                                                                                  .representativePrice(snapshot.representativePrice())
                                                                                                  .zoneLow(snapshot.zoneLow())
                                                                                                  .zoneHigh(snapshot.zoneHigh())
                                                                                                  .distanceFromCurrent(snapshot.distanceFromCurrent())
                                                                                                  .distanceToZone(snapshot.distanceToZone())
                                                                                                  .zoneStrengthScore(snapshot.zoneStrengthScore())
                                                                                                  .interactionType(snapshot.interactionType().name())
                                                                                                  .strongestLevelLabel(snapshot.strongestLevelLabel().name())
                                                                                                  .strongestSourceType(snapshot.strongestSourceType().name())
                                                                                                  .levelCount(snapshot.levelCount())
                                                                                                  .recentTestCount(snapshot.recentTestCount())
                                                                                                  .recentRejectionCount(snapshot.recentRejectionCount())
                                                                                                  .recentBreakCount(snapshot.recentBreakCount())
                                                                                                  .includedLevelLabelsPayload(includedLevelLabelsPayload)
                                                                                                  .includedSourceTypesPayload(includedSourceTypesPayload)
                                                                                                  .triggerFactsPayload(triggerFactsPayload)
                                                                                                  .sourceDataVersion(snapshot.sourceDataVersion())
                                                                                                  .build();
            return marketCandidateLevelZoneSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.currentPrice(),
                snapshot.representativePrice(),
                snapshot.zoneLow(),
                snapshot.zoneHigh(),
                snapshot.distanceFromCurrent(),
                snapshot.distanceToZone(),
                snapshot.zoneStrengthScore(),
                snapshot.interactionType().name(),
                snapshot.strongestLevelLabel().name(),
                snapshot.strongestSourceType().name(),
                snapshot.levelCount(),
                snapshot.recentTestCount(),
                snapshot.recentRejectionCount(),
                snapshot.recentBreakCount(),
                includedLevelLabelsPayload,
                includedSourceTypesPayload,
                triggerFactsPayload,
                snapshot.sourceDataVersion()
        );
        return existingEntity;
    }

    private String writePayload(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize market candidate level zone payload.", exception);
        }
    }
}

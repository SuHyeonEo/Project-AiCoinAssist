package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketCandidateLevelSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketCandidateLevelSnapshotPersistenceService {

    private final MarketCandidateLevelSnapshotService marketCandidateLevelSnapshotService;
    private final MarketCandidateLevelSnapshotRepository marketCandidateLevelSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<MarketCandidateLevelSnapshotEntity> createAndSaveAll(MarketIndicatorSnapshotEntity currentSnapshot) {
        List<MarketCandidateLevelSnapshotEntity> entities = new ArrayList<>();
        for (MarketCandidateLevelSnapshot snapshot : marketCandidateLevelSnapshotService.createAll(currentSnapshot)) {
            entities.add(createAndSave(snapshot));
        }
        return entities;
    }

    @Transactional
    public MarketCandidateLevelSnapshotEntity createAndSave(MarketCandidateLevelSnapshot snapshot) {
        MarketCandidateLevelSnapshotEntity existingEntity = marketCandidateLevelSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeAndLevelTypeAndLevelLabelOrderByIdDesc(
                        snapshot.symbol(),
                        snapshot.intervalValue(),
                        snapshot.snapshotTime(),
                        snapshot.levelType().name(),
                        snapshot.levelLabel().name()
                )
                .orElse(null);

        String triggerFactsPayload = triggerFactsPayload(snapshot.triggerFacts());
        if (existingEntity == null) {
            MarketCandidateLevelSnapshotEntity entity = MarketCandidateLevelSnapshotEntity.builder()
                                                                                          .symbol(snapshot.symbol())
                                                                                          .intervalValue(snapshot.intervalValue())
                                                                                          .snapshotTime(snapshot.snapshotTime())
                                                                                          .levelType(snapshot.levelType().name())
                                                                                          .levelLabel(snapshot.levelLabel().name())
                                                                                          .sourceType(snapshot.sourceType().name())
                                                                                          .currentPrice(snapshot.currentPrice())
                                                                                          .levelPrice(snapshot.levelPrice())
                                                                                          .distanceFromCurrent(snapshot.distanceFromCurrent())
                                                                                          .strengthScore(snapshot.strengthScore())
                                                                                          .rationale(snapshot.rationale())
                                                                                          .triggerFactsPayload(triggerFactsPayload)
                                                                                          .sourceDataVersion(snapshot.sourceDataVersion())
                                                                                          .build();
            return marketCandidateLevelSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.currentPrice(),
                snapshot.levelPrice(),
                snapshot.distanceFromCurrent(),
                snapshot.strengthScore(),
                snapshot.rationale(),
                triggerFactsPayload,
                snapshot.sourceDataVersion()
        );
        return existingEntity;
    }

    private String triggerFactsPayload(List<String> triggerFacts) {
        try {
            return objectMapper.writeValueAsString(triggerFacts);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize market candidate level trigger facts.", exception);
        }
    }
}

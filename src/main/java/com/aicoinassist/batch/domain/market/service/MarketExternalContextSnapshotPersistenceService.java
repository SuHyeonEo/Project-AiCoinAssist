package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketExternalContextSnapshotPersistenceService {

    private final MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MarketExternalContextSnapshotEntity createAndSave(MarketExternalContextSnapshot snapshot) {
        MarketExternalContextSnapshotEntity existingEntity = marketExternalContextSnapshotRepository
                .findTopBySymbolAndSnapshotTimeOrderByIdDesc(snapshot.symbol(), snapshot.snapshotTime())
                .orElse(null);

        String regimeSignalsPayload = serialize(snapshot);
        String dominantDirection = enumName(snapshot.dominantDirection());
        String highestSeverity = enumName(snapshot.highestSeverity());
        String primarySignalCategory = enumName(snapshot.primarySignalCategory());

        if (existingEntity == null) {
            MarketExternalContextSnapshotEntity entity = MarketExternalContextSnapshotEntity.builder()
                                                                                            .symbol(snapshot.symbol())
                                                                                            .snapshotTime(snapshot.snapshotTime())
                                                                                            .derivativeSnapshotTime(snapshot.derivativeSnapshotTime())
                                                                                            .macroSnapshotTime(snapshot.macroSnapshotTime())
                                                                                            .sentimentSnapshotTime(snapshot.sentimentSnapshotTime())
                                                                                            .onchainSnapshotTime(snapshot.onchainSnapshotTime())
                                                                                            .sourceDataVersion(snapshot.sourceDataVersion())
                                                                                            .compositeRiskScore(snapshot.compositeRiskScore())
                                                                                            .dominantDirection(dominantDirection)
                                                                                            .highestSeverity(highestSeverity)
                                                                                            .supportiveSignalCount(snapshot.supportiveSignalCount())
                                                                                            .cautionarySignalCount(snapshot.cautionarySignalCount())
                                                                                            .headwindSignalCount(snapshot.headwindSignalCount())
                                                                                            .primarySignalCategory(primarySignalCategory)
                                                                                            .primarySignalTitle(snapshot.primarySignalTitle())
                                                                                            .primarySignalDetail(snapshot.primarySignalDetail())
                                                                                            .regimeSignalsPayload(regimeSignalsPayload)
                                                                                            .build();
            return marketExternalContextSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.derivativeSnapshotTime(),
                snapshot.macroSnapshotTime(),
                snapshot.sentimentSnapshotTime(),
                snapshot.onchainSnapshotTime(),
                snapshot.sourceDataVersion(),
                snapshot.compositeRiskScore(),
                dominantDirection,
                highestSeverity,
                snapshot.supportiveSignalCount(),
                snapshot.cautionarySignalCount(),
                snapshot.headwindSignalCount(),
                primarySignalCategory,
                snapshot.primarySignalTitle(),
                snapshot.primarySignalDetail(),
                regimeSignalsPayload
        );
        return existingEntity;
    }

    private String serialize(MarketExternalContextSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot.regimeSignals());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize external context regime signals.", exception);
        }
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}

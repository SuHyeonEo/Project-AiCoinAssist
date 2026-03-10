package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.onchain.dto.OnchainFactSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainFactSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnchainFactSnapshotPersistenceService {

    private final OnchainFactSnapshotService onchainFactSnapshotService;
    private final OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    @Transactional
    public OnchainFactSnapshotEntity createAndSave(String symbol) {
        OnchainFactSnapshot snapshot = onchainFactSnapshotService.create(symbol);

        OnchainFactSnapshotEntity existingEntity = onchainFactSnapshotRepository
                .findTopBySymbolAndSnapshotTimeOrderByIdDesc(symbol, snapshot.snapshotTime())
                .orElse(null);

        if (existingEntity == null) {
            OnchainFactSnapshotEntity entity = OnchainFactSnapshotEntity.builder()
                                                                       .symbol(snapshot.symbol())
                                                                       .assetCode(snapshot.assetCode())
                                                                       .snapshotTime(snapshot.snapshotTime())
                                                                       .activeAddressSourceEventTime(snapshot.activeAddressSourceEventTime())
                                                                       .transactionCountSourceEventTime(snapshot.transactionCountSourceEventTime())
                                                                       .marketCapSourceEventTime(snapshot.marketCapSourceEventTime())
                                                                       .sourceDataVersion(snapshot.sourceDataVersion())
                                                                       .activeAddressCount(snapshot.activeAddressCount())
                                                                       .transactionCount(snapshot.transactionCount())
                                                                       .marketCapUsd(snapshot.marketCapUsd())
                                                                       .build();
            return onchainFactSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.activeAddressSourceEventTime(),
                snapshot.transactionCountSourceEventTime(),
                snapshot.marketCapSourceEventTime(),
                snapshot.sourceDataVersion(),
                snapshot.activeAddressCount(),
                snapshot.transactionCount(),
                snapshot.marketCapUsd()
        );
        return existingEntity;
    }
}

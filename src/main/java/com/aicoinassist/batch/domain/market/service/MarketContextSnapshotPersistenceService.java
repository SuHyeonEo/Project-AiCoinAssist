package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketContextSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketContextSnapshotPersistenceService {

    private final MarketContextSnapshotService marketContextSnapshotService;
    private final MarketContextSnapshotRepository marketContextSnapshotRepository;

    @Transactional
    public MarketContextSnapshotEntity createAndSave(String symbol) {
        MarketContextSnapshot snapshot = marketContextSnapshotService.create(symbol);

        MarketContextSnapshotEntity existingEntity = marketContextSnapshotRepository
                .findTopBySymbolAndSnapshotTimeOrderByIdDesc(symbol, snapshot.snapshotTime())
                .orElse(null);

        if (existingEntity == null) {
            MarketContextSnapshotEntity entity = MarketContextSnapshotEntity.builder()
                                                                            .symbol(symbol)
                                                                            .snapshotTime(snapshot.snapshotTime())
                                                                            .openInterestSourceEventTime(snapshot.openInterestSourceEventTime())
                                                                            .premiumIndexSourceEventTime(snapshot.premiumIndexSourceEventTime())
                                                                            .sourceDataVersion(snapshot.sourceDataVersion())
                                                                            .openInterest(snapshot.openInterest())
                                                                            .markPrice(snapshot.markPrice())
                                                                            .indexPrice(snapshot.indexPrice())
                                                                            .lastFundingRate(snapshot.lastFundingRate())
                                                                            .nextFundingTime(snapshot.nextFundingTime())
                                                                            .markIndexBasisRate(snapshot.markIndexBasisRate())
                                                                            .build();
            return marketContextSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.openInterestSourceEventTime(),
                snapshot.premiumIndexSourceEventTime(),
                snapshot.sourceDataVersion(),
                snapshot.openInterest(),
                snapshot.markPrice(),
                snapshot.indexPrice(),
                snapshot.lastFundingRate(),
                snapshot.nextFundingTime(),
                snapshot.markIndexBasisRate()
        );

        return existingEntity;
    }
}

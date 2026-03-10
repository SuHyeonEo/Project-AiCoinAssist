package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketLevelContextSnapshotRepository extends JpaRepository<MarketLevelContextSnapshotEntity, Long> {

    Optional<MarketLevelContextSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );

    Optional<MarketLevelContextSnapshotEntity> findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue
    );
}

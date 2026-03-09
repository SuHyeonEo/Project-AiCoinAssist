package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketContextSnapshotRepository extends JpaRepository<MarketContextSnapshotEntity, Long> {

    Optional<MarketContextSnapshotEntity> findTopBySymbolAndSnapshotTimeOrderByIdDesc(
            String symbol,
            Instant snapshotTime
    );

    Optional<MarketContextSnapshotEntity> findTopBySymbolOrderBySnapshotTimeDescIdDesc(String symbol);
}

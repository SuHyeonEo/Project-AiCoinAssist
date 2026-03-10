package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketExternalContextSnapshotRepository extends JpaRepository<MarketExternalContextSnapshotEntity, Long> {

    Optional<MarketExternalContextSnapshotEntity> findTopBySymbolAndSnapshotTimeOrderByIdDesc(
            String symbol,
            Instant snapshotTime
    );
}

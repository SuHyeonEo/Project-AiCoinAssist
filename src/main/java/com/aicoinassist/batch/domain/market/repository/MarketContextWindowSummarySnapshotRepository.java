package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketContextWindowSummarySnapshotRepository extends JpaRepository<MarketContextWindowSummarySnapshotEntity, Long> {

    Optional<MarketContextWindowSummarySnapshotEntity> findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
            String symbol,
            String windowType,
            Instant windowEndTime
    );
}

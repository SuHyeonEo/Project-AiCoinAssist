package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextWindowSummarySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketExternalContextWindowSummarySnapshotRepository extends JpaRepository<MarketExternalContextWindowSummarySnapshotEntity, Long> {

    Optional<MarketExternalContextWindowSummarySnapshotEntity> findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
            String symbol,
            String windowType,
            Instant windowEndTime
    );
}

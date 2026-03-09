package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketWindowSummarySnapshotRepository extends JpaRepository<MarketWindowSummarySnapshotEntity, Long> {

    Optional<MarketWindowSummarySnapshotEntity> findTopBySymbolAndIntervalValueAndWindowTypeAndWindowEndTimeOrderByIdDesc(
            String symbol,
            String intervalValue,
            String windowType,
            Instant windowEndTime
    );

    List<MarketWindowSummarySnapshotEntity> findAllBySymbolAndIntervalValueAndWindowEndTimeOrderByWindowTypeAsc(
            String symbol,
            String intervalValue,
            Instant windowEndTime
    );
}

package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketIndicatorSnapshotRepository extends JpaRepository<MarketIndicatorSnapshotEntity, Long> {

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue
    );

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceDescSnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTimeFrom,
            Instant snapshotTimeTo
    );

    Optional<MarketIndicatorSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceAscSnapshotTimeDescIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTimeFrom,
            Instant snapshotTimeTo
    );
}

package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketCandidateLevelZoneSnapshotRepository extends JpaRepository<MarketCandidateLevelZoneSnapshotEntity, Long> {

    Optional<MarketCandidateLevelZoneSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeAndZoneTypeAndZoneRankOrderByIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            String zoneType,
            Integer zoneRank
    );

    List<MarketCandidateLevelZoneSnapshotEntity> findAllBySymbolAndIntervalValueAndSnapshotTimeOrderByZoneTypeAscZoneRankAsc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );
}

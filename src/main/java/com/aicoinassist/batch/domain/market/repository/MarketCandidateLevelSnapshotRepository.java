package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketCandidateLevelSnapshotRepository extends JpaRepository<MarketCandidateLevelSnapshotEntity, Long> {

    Optional<MarketCandidateLevelSnapshotEntity> findTopBySymbolAndIntervalValueAndSnapshotTimeAndLevelTypeAndLevelLabelOrderByIdDesc(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            String levelType,
            String levelLabel
    );

    List<MarketCandidateLevelSnapshotEntity> findAllBySymbolAndIntervalValueAndSnapshotTimeOrderByLevelTypeAscLevelPriceAsc(
            String symbol,
            String intervalValue,
            Instant snapshotTime
    );
}

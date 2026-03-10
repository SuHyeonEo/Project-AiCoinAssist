package com.aicoinassist.batch.domain.onchain.repository;

import com.aicoinassist.batch.domain.onchain.entity.OnchainWindowSummarySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OnchainWindowSummarySnapshotRepository extends JpaRepository<OnchainWindowSummarySnapshotEntity, Long> {

    Optional<OnchainWindowSummarySnapshotEntity> findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
            String symbol,
            String windowType,
            Instant windowEndTime
    );
}

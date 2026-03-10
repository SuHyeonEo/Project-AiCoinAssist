package com.aicoinassist.batch.domain.onchain.repository;

import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OnchainFactSnapshotRepository extends JpaRepository<OnchainFactSnapshotEntity, Long> {

    Optional<OnchainFactSnapshotEntity> findTopBySymbolAndSnapshotTimeOrderByIdDesc(
            String symbol,
            Instant snapshotTime
    );
}

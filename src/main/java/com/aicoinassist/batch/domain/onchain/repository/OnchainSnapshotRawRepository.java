package com.aicoinassist.batch.domain.onchain.repository;

import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OnchainSnapshotRawRepository extends JpaRepository<OnchainSnapshotRawEntity, Long> {

    Optional<OnchainSnapshotRawEntity> findTopBySourceAndAssetCodeAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
            String source,
            String assetCode,
            OnchainMetricType metricType,
            Instant sourceEventTime
    );

    Optional<OnchainSnapshotRawEntity> findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(
            String assetCode,
            OnchainMetricType metricType
    );
}

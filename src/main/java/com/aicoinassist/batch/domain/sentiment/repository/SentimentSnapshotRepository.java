package com.aicoinassist.batch.domain.sentiment.repository;

import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SentimentSnapshotRepository extends JpaRepository<SentimentSnapshotEntity, Long> {

    Optional<SentimentSnapshotEntity> findTopByMetricTypeAndSnapshotTimeOrderByIdDesc(
            SentimentMetricType metricType,
            Instant snapshotTime
    );

    Optional<SentimentSnapshotEntity> findTopByMetricTypeAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
            SentimentMetricType metricType,
            Instant snapshotTime
    );
}

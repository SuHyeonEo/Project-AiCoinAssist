package com.aicoinassist.batch.domain.sentiment.repository;

import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SentimentSnapshotRawRepository extends JpaRepository<SentimentSnapshotRawEntity, Long> {

    Optional<SentimentSnapshotRawEntity> findTopBySourceAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
            String source,
            SentimentMetricType metricType,
            Instant sourceEventTime
    );

    Optional<SentimentSnapshotRawEntity> findTopByMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(
            SentimentMetricType metricType
    );
}

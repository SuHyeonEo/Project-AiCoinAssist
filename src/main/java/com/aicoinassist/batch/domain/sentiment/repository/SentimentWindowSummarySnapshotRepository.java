package com.aicoinassist.batch.domain.sentiment.repository;

import com.aicoinassist.batch.domain.sentiment.entity.SentimentWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SentimentWindowSummarySnapshotRepository extends JpaRepository<SentimentWindowSummarySnapshotEntity, Long> {

    Optional<SentimentWindowSummarySnapshotEntity> findTopByMetricTypeAndWindowTypeAndWindowEndTimeOrderByIdDesc(
            SentimentMetricType metricType,
            String windowType,
            Instant windowEndTime
    );
}

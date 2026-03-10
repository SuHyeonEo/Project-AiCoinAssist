package com.aicoinassist.batch.domain.sentiment.entity;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(
        name = "sentiment_snapshot_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sentiment_snapshot_raw_source_metric_source_event_time",
                        columnNames = {"source", "metric_type", "source_event_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_sentiment_snapshot_raw_metric_collected_time",
                        columnList = "metric_type, collected_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentimentSnapshotRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SentimentMetricType metricType;

    @Column
    private Instant sourceEventTime;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(columnDefinition = "TEXT")
    private String validationDetails;

    @Column(precision = 19, scale = 8)
    private BigDecimal indexValue;

    @Column(length = 50)
    private String classification;

    @Column
    private Long timeUntilUpdateSeconds;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Builder
    public SentimentSnapshotRawEntity(
            String source,
            SentimentMetricType metricType,
            Instant sourceEventTime,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal indexValue,
            String classification,
            Long timeUntilUpdateSeconds,
            String rawPayload
    ) {
        this.source = source;
        this.metricType = metricType;
        this.sourceEventTime = sourceEventTime;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.indexValue = indexValue;
        this.classification = classification;
        this.timeUntilUpdateSeconds = timeUntilUpdateSeconds;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal indexValue,
            String classification,
            Long timeUntilUpdateSeconds,
            String rawPayload
    ) {
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.indexValue = indexValue;
        this.classification = classification;
        this.timeUntilUpdateSeconds = timeUntilUpdateSeconds;
        this.rawPayload = rawPayload;
    }
}

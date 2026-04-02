package com.aicoinassist.batch.domain.sentiment.entity;

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
        name = "sentiment_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sentiment_snapshot_metric_snapshot_time",
                        columnNames = {"metric_type", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_sentiment_snapshot_metric_snapshot_time",
                        columnList = "metric_type, snapshot_time"
                ),
                @Index(
                        name = "idx_sentiment_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentimentSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SentimentMetricType metricType;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false)
    private Instant sourceEventTime;

    @Column(nullable = false, length = 200)
    private String sourceDataVersion;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal indexValue;

    @Column(nullable = false, length = 50)
    private String classification;

    @Column
    private Long timeUntilUpdateSeconds;

    @Column
    private Instant previousSnapshotTime;

    @Column(precision = 19, scale = 8)
    private BigDecimal previousIndexValue;

    @Column(precision = 19, scale = 8)
    private BigDecimal valueChange;

    @Column(precision = 19, scale = 8)
    private BigDecimal valueChangeRate;

    @Column
    private Boolean classificationChanged;

    @Builder
    public SentimentSnapshotEntity(
            SentimentMetricType metricType,
            Instant snapshotTime,
            Instant sourceEventTime,
            String sourceDataVersion,
            BigDecimal indexValue,
            String classification,
            Long timeUntilUpdateSeconds,
            Instant previousSnapshotTime,
            BigDecimal previousIndexValue,
            BigDecimal valueChange,
            BigDecimal valueChangeRate,
            Boolean classificationChanged
    ) {
        this.metricType = metricType;
        this.snapshotTime = snapshotTime;
        this.sourceEventTime = sourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.indexValue = indexValue;
        this.classification = classification;
        this.timeUntilUpdateSeconds = timeUntilUpdateSeconds;
        this.previousSnapshotTime = previousSnapshotTime;
        this.previousIndexValue = previousIndexValue;
        this.valueChange = valueChange;
        this.valueChangeRate = valueChangeRate;
        this.classificationChanged = classificationChanged;
    }

    public void refreshFromSnapshot(
            Instant sourceEventTime,
            String sourceDataVersion,
            BigDecimal indexValue,
            String classification,
            Long timeUntilUpdateSeconds,
            Instant previousSnapshotTime,
            BigDecimal previousIndexValue,
            BigDecimal valueChange,
            BigDecimal valueChangeRate,
            Boolean classificationChanged
    ) {
        this.sourceEventTime = sourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.indexValue = indexValue;
        this.classification = classification;
        this.timeUntilUpdateSeconds = timeUntilUpdateSeconds;
        this.previousSnapshotTime = previousSnapshotTime;
        this.previousIndexValue = previousIndexValue;
        this.valueChange = valueChange;
        this.valueChangeRate = valueChangeRate;
        this.classificationChanged = classificationChanged;
    }
}

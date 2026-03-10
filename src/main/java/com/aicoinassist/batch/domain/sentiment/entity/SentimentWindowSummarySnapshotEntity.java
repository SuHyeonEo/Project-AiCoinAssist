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
        name = "sentiment_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sentiment_window_summary_metric_window_end",
                        columnNames = {"metric_type", "window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_sentiment_window_summary_metric_window_end",
                        columnList = "metric_type, window_end_time"
                ),
                @Index(
                        name = "idx_sentiment_window_summary_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentimentWindowSummarySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SentimentMetricType metricType;

    @Column(nullable = false, length = 20)
    private String windowType;

    @Column(nullable = false)
    private Instant windowStartTime;

    @Column(nullable = false)
    private Instant windowEndTime;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentIndexValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageIndexValue;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentIndexVsAverage;

    @Column(nullable = false, length = 50)
    private String currentClassification;

    @Column(nullable = false)
    private Integer greedSampleCount;

    @Column(nullable = false)
    private Integer fearSampleCount;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public SentimentWindowSummarySnapshotEntity(
            SentimentMetricType metricType,
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentIndexValue,
            BigDecimal averageIndexValue,
            BigDecimal currentIndexVsAverage,
            String currentClassification,
            Integer greedSampleCount,
            Integer fearSampleCount,
            String sourceDataVersion
    ) {
        this.metricType = metricType;
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentIndexValue = currentIndexValue;
        this.averageIndexValue = averageIndexValue;
        this.currentIndexVsAverage = currentIndexVsAverage;
        this.currentClassification = currentClassification;
        this.greedSampleCount = greedSampleCount;
        this.fearSampleCount = fearSampleCount;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentIndexValue,
            BigDecimal averageIndexValue,
            BigDecimal currentIndexVsAverage,
            String currentClassification,
            Integer greedSampleCount,
            Integer fearSampleCount,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentIndexValue = currentIndexValue;
        this.averageIndexValue = averageIndexValue;
        this.currentIndexVsAverage = currentIndexVsAverage;
        this.currentClassification = currentClassification;
        this.greedSampleCount = greedSampleCount;
        this.fearSampleCount = fearSampleCount;
        this.sourceDataVersion = sourceDataVersion;
    }
}

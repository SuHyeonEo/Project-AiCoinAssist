package com.aicoinassist.batch.domain.macro.entity;

import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
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
import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "macro_snapshot_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_macro_snapshot_raw_source_metric_observation_date",
                        columnNames = {"source", "metric_type", "observation_date"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_macro_snapshot_raw_metric_collected_time",
                        columnList = "metric_type, collected_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MacroSnapshotRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MacroMetricType metricType;

    @Column(length = 50)
    private String seriesId;

    @Column(length = 50)
    private String units;

    @Column
    private LocalDate observationDate;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(columnDefinition = "TEXT")
    private String validationDetails;

    @Column(precision = 19, scale = 8)
    private BigDecimal metricValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Builder
    public MacroSnapshotRawEntity(
            String source,
            MacroMetricType metricType,
            String seriesId,
            String units,
            LocalDate observationDate,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal metricValue,
            String rawPayload
    ) {
        this.source = source;
        this.metricType = metricType;
        this.seriesId = seriesId;
        this.units = units;
        this.observationDate = observationDate;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.metricValue = metricValue;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            String seriesId,
            String units,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal metricValue,
            String rawPayload
    ) {
        this.seriesId = seriesId;
        this.units = units;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.metricValue = metricValue;
        this.rawPayload = rawPayload;
    }
}

package com.aicoinassist.batch.domain.onchain.entity;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
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
        name = "onchain_snapshot_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_onchain_snapshot_raw_source_asset_metric_source_event_time",
                        columnNames = {"source", "asset_code", "metric_type", "source_event_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_onchain_snapshot_raw_asset_metric_collected_time",
                        columnList = "asset_code, metric_type, collected_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnchainSnapshotRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, length = 20)
    private String assetCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OnchainMetricType metricType;

    @Column
    private Instant sourceEventTime;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(columnDefinition = "TEXT")
    private String validationDetails;

    @Column(precision = 24, scale = 8)
    private BigDecimal metricValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Builder
    public OnchainSnapshotRawEntity(
            String source,
            String assetCode,
            OnchainMetricType metricType,
            Instant sourceEventTime,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal metricValue,
            String rawPayload
    ) {
        this.source = source;
        this.assetCode = assetCode;
        this.metricType = metricType;
        this.sourceEventTime = sourceEventTime;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.metricValue = metricValue;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal metricValue,
            String rawPayload
    ) {
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.metricValue = metricValue;
        this.rawPayload = rawPayload;
    }
}

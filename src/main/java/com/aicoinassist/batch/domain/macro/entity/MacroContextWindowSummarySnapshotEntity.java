package com.aicoinassist.batch.domain.macro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "macro_context_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_macro_context_window_summary_window_end",
                        columnNames = {"window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_macro_context_window_summary_window_end",
                        columnList = "window_end_time"
                ),
                @Index(
                        name = "idx_macro_context_window_summary_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MacroContextWindowSummarySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String windowType;

    @Column(nullable = false)
    private Instant windowStartTime;

    @Column(nullable = false)
    private Instant windowEndTime;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentDxyProxyValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageDxyProxyValue;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentDxyProxyVsAverage;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentUs10yYieldValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageUs10yYieldValue;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentUs10yYieldVsAverage;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentUsdKrwValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageUsdKrwValue;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentUsdKrwVsAverage;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public MacroContextWindowSummarySnapshotEntity(
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentDxyProxyValue,
            BigDecimal averageDxyProxyValue,
            BigDecimal currentDxyProxyVsAverage,
            BigDecimal currentUs10yYieldValue,
            BigDecimal averageUs10yYieldValue,
            BigDecimal currentUs10yYieldVsAverage,
            BigDecimal currentUsdKrwValue,
            BigDecimal averageUsdKrwValue,
            BigDecimal currentUsdKrwVsAverage,
            String sourceDataVersion
    ) {
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentDxyProxyValue = currentDxyProxyValue;
        this.averageDxyProxyValue = averageDxyProxyValue;
        this.currentDxyProxyVsAverage = currentDxyProxyVsAverage;
        this.currentUs10yYieldValue = currentUs10yYieldValue;
        this.averageUs10yYieldValue = averageUs10yYieldValue;
        this.currentUs10yYieldVsAverage = currentUs10yYieldVsAverage;
        this.currentUsdKrwValue = currentUsdKrwValue;
        this.averageUsdKrwValue = averageUsdKrwValue;
        this.currentUsdKrwVsAverage = currentUsdKrwVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentDxyProxyValue,
            BigDecimal averageDxyProxyValue,
            BigDecimal currentDxyProxyVsAverage,
            BigDecimal currentUs10yYieldValue,
            BigDecimal averageUs10yYieldValue,
            BigDecimal currentUs10yYieldVsAverage,
            BigDecimal currentUsdKrwValue,
            BigDecimal averageUsdKrwValue,
            BigDecimal currentUsdKrwVsAverage,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentDxyProxyValue = currentDxyProxyValue;
        this.averageDxyProxyValue = averageDxyProxyValue;
        this.currentDxyProxyVsAverage = currentDxyProxyVsAverage;
        this.currentUs10yYieldValue = currentUs10yYieldValue;
        this.averageUs10yYieldValue = averageUs10yYieldValue;
        this.currentUs10yYieldVsAverage = currentUs10yYieldVsAverage;
        this.currentUsdKrwValue = currentUsdKrwValue;
        this.averageUsdKrwValue = averageUsdKrwValue;
        this.currentUsdKrwVsAverage = currentUsdKrwVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }
}

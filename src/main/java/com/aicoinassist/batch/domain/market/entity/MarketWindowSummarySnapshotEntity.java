package com.aicoinassist.batch.domain.market.entity;

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
        name = "market_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_window_summary_snapshot_symbol_interval_window_end",
                        columnNames = {"symbol", "interval_value", "window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_window_summary_snapshot_symbol_interval_window_end",
                        columnList = "symbol, interval_value, window_end_time"
                ),
                @Index(
                        name = "idx_market_window_summary_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketWindowSummarySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String intervalValue;

    @Column(nullable = false, length = 20)
    private String windowType;

    @Column(nullable = false)
    private Instant windowStartTime;

    @Column(nullable = false)
    private Instant windowEndTime;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal windowHigh;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal windowLow;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal windowRange;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentPositionInRange;

    @Column(precision = 19, scale = 8)
    private BigDecimal distanceFromWindowHigh;

    @Column(precision = 19, scale = 8)
    private BigDecimal reboundFromWindowLow;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageVolume;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageAtr;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentVolume;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentAtr;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentVolumeVsAverage;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentAtrVsAverage;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public MarketWindowSummarySnapshotEntity(
            String symbol,
            String intervalValue,
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentPrice,
            BigDecimal windowHigh,
            BigDecimal windowLow,
            BigDecimal windowRange,
            BigDecimal currentPositionInRange,
            BigDecimal distanceFromWindowHigh,
            BigDecimal reboundFromWindowLow,
            BigDecimal averageVolume,
            BigDecimal averageAtr,
            BigDecimal currentVolume,
            BigDecimal currentAtr,
            BigDecimal currentVolumeVsAverage,
            BigDecimal currentAtrVsAverage,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentPrice = currentPrice;
        this.windowHigh = windowHigh;
        this.windowLow = windowLow;
        this.windowRange = windowRange;
        this.currentPositionInRange = currentPositionInRange;
        this.distanceFromWindowHigh = distanceFromWindowHigh;
        this.reboundFromWindowLow = reboundFromWindowLow;
        this.averageVolume = averageVolume;
        this.averageAtr = averageAtr;
        this.currentVolume = currentVolume;
        this.currentAtr = currentAtr;
        this.currentVolumeVsAverage = currentVolumeVsAverage;
        this.currentAtrVsAverage = currentAtrVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentPrice,
            BigDecimal windowHigh,
            BigDecimal windowLow,
            BigDecimal windowRange,
            BigDecimal currentPositionInRange,
            BigDecimal distanceFromWindowHigh,
            BigDecimal reboundFromWindowLow,
            BigDecimal averageVolume,
            BigDecimal averageAtr,
            BigDecimal currentVolume,
            BigDecimal currentAtr,
            BigDecimal currentVolumeVsAverage,
            BigDecimal currentAtrVsAverage,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentPrice = currentPrice;
        this.windowHigh = windowHigh;
        this.windowLow = windowLow;
        this.windowRange = windowRange;
        this.currentPositionInRange = currentPositionInRange;
        this.distanceFromWindowHigh = distanceFromWindowHigh;
        this.reboundFromWindowLow = reboundFromWindowLow;
        this.averageVolume = averageVolume;
        this.averageAtr = averageAtr;
        this.currentVolume = currentVolume;
        this.currentAtr = currentAtr;
        this.currentVolumeVsAverage = currentVolumeVsAverage;
        this.currentAtrVsAverage = currentAtrVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }
}

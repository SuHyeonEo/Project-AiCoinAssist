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
        name = "market_external_context_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_external_context_window_summary_symbol_window_end",
                        columnNames = {"symbol", "window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_external_context_window_summary_symbol_window_end",
                        columnList = "symbol, window_end_time"
                ),
                @Index(
                        name = "idx_market_external_context_window_summary_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketExternalContextWindowSummarySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String windowType;

    @Column(nullable = false)
    private Instant windowStartTime;

    @Column(nullable = false)
    private Instant windowEndTime;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentCompositeRiskScore;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageCompositeRiskScore;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentCompositeRiskVsAverage;

    @Column(nullable = false)
    private Integer supportiveDominanceSampleCount;

    @Column(nullable = false)
    private Integer cautionaryDominanceSampleCount;

    @Column(nullable = false)
    private Integer headwindDominanceSampleCount;

    @Column(nullable = false)
    private Integer highSeveritySampleCount;

    @Column(nullable = false, length = 500)
    private String sourceDataVersion;

    @Builder
    public MarketExternalContextWindowSummarySnapshotEntity(
            String symbol,
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentCompositeRiskScore,
            BigDecimal averageCompositeRiskScore,
            BigDecimal currentCompositeRiskVsAverage,
            Integer supportiveDominanceSampleCount,
            Integer cautionaryDominanceSampleCount,
            Integer headwindDominanceSampleCount,
            Integer highSeveritySampleCount,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentCompositeRiskScore = currentCompositeRiskScore;
        this.averageCompositeRiskScore = averageCompositeRiskScore;
        this.currentCompositeRiskVsAverage = currentCompositeRiskVsAverage;
        this.supportiveDominanceSampleCount = supportiveDominanceSampleCount;
        this.cautionaryDominanceSampleCount = cautionaryDominanceSampleCount;
        this.headwindDominanceSampleCount = headwindDominanceSampleCount;
        this.highSeveritySampleCount = highSeveritySampleCount;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentCompositeRiskScore,
            BigDecimal averageCompositeRiskScore,
            BigDecimal currentCompositeRiskVsAverage,
            Integer supportiveDominanceSampleCount,
            Integer cautionaryDominanceSampleCount,
            Integer headwindDominanceSampleCount,
            Integer highSeveritySampleCount,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentCompositeRiskScore = currentCompositeRiskScore;
        this.averageCompositeRiskScore = averageCompositeRiskScore;
        this.currentCompositeRiskVsAverage = currentCompositeRiskVsAverage;
        this.supportiveDominanceSampleCount = supportiveDominanceSampleCount;
        this.cautionaryDominanceSampleCount = cautionaryDominanceSampleCount;
        this.headwindDominanceSampleCount = headwindDominanceSampleCount;
        this.highSeveritySampleCount = highSeveritySampleCount;
        this.sourceDataVersion = sourceDataVersion;
    }
}

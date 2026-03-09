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
        name = "market_context_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_context_window_summary_symbol_window_end",
                        columnNames = {"symbol", "window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_context_window_summary_symbol_window_end",
                        columnList = "symbol, window_end_time"
                ),
                @Index(
                        name = "idx_market_context_window_summary_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketContextWindowSummarySnapshotEntity {

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
    private BigDecimal currentOpenInterest;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageOpenInterest;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentOpenInterestVsAverage;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentFundingRate;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageFundingRate;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentFundingVsAverage;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentBasisRate;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageBasisRate;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentBasisVsAverage;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public MarketContextWindowSummarySnapshotEntity(
            String symbol,
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentOpenInterest,
            BigDecimal averageOpenInterest,
            BigDecimal currentOpenInterestVsAverage,
            BigDecimal currentFundingRate,
            BigDecimal averageFundingRate,
            BigDecimal currentFundingVsAverage,
            BigDecimal currentBasisRate,
            BigDecimal averageBasisRate,
            BigDecimal currentBasisVsAverage,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentOpenInterest = currentOpenInterest;
        this.averageOpenInterest = averageOpenInterest;
        this.currentOpenInterestVsAverage = currentOpenInterestVsAverage;
        this.currentFundingRate = currentFundingRate;
        this.averageFundingRate = averageFundingRate;
        this.currentFundingVsAverage = currentFundingVsAverage;
        this.currentBasisRate = currentBasisRate;
        this.averageBasisRate = averageBasisRate;
        this.currentBasisVsAverage = currentBasisVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentOpenInterest,
            BigDecimal averageOpenInterest,
            BigDecimal currentOpenInterestVsAverage,
            BigDecimal currentFundingRate,
            BigDecimal averageFundingRate,
            BigDecimal currentFundingVsAverage,
            BigDecimal currentBasisRate,
            BigDecimal averageBasisRate,
            BigDecimal currentBasisVsAverage,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentOpenInterest = currentOpenInterest;
        this.averageOpenInterest = averageOpenInterest;
        this.currentOpenInterestVsAverage = currentOpenInterestVsAverage;
        this.currentFundingRate = currentFundingRate;
        this.averageFundingRate = averageFundingRate;
        this.currentFundingVsAverage = currentFundingVsAverage;
        this.currentBasisRate = currentBasisRate;
        this.averageBasisRate = averageBasisRate;
        this.currentBasisVsAverage = currentBasisVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }
}

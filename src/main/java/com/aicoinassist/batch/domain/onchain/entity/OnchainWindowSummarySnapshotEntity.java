package com.aicoinassist.batch.domain.onchain.entity;

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
        name = "onchain_window_summary_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_onchain_window_summary_symbol_window_end",
                        columnNames = {"symbol", "window_type", "window_end_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_onchain_window_summary_symbol_window_end",
                        columnList = "symbol, window_end_time"
                ),
                @Index(
                        name = "idx_onchain_window_summary_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnchainWindowSummarySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String assetCode;

    @Column(nullable = false, length = 20)
    private String windowType;

    @Column(nullable = false)
    private Instant windowStartTime;

    @Column(nullable = false)
    private Instant windowEndTime;

    @Column(nullable = false)
    private Integer sampleCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentActiveAddressCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageActiveAddressCount;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentActiveAddressVsAverage;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentTransactionCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal averageTransactionCount;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentTransactionCountVsAverage;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal currentMarketCapUsd;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal averageMarketCapUsd;

    @Column(precision = 19, scale = 8)
    private BigDecimal currentMarketCapVsAverage;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public OnchainWindowSummarySnapshotEntity(
            String symbol,
            String assetCode,
            String windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            Integer sampleCount,
            BigDecimal currentActiveAddressCount,
            BigDecimal averageActiveAddressCount,
            BigDecimal currentActiveAddressVsAverage,
            BigDecimal currentTransactionCount,
            BigDecimal averageTransactionCount,
            BigDecimal currentTransactionCountVsAverage,
            BigDecimal currentMarketCapUsd,
            BigDecimal averageMarketCapUsd,
            BigDecimal currentMarketCapVsAverage,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.assetCode = assetCode;
        this.windowType = windowType;
        this.windowStartTime = windowStartTime;
        this.windowEndTime = windowEndTime;
        this.sampleCount = sampleCount;
        this.currentActiveAddressCount = currentActiveAddressCount;
        this.averageActiveAddressCount = averageActiveAddressCount;
        this.currentActiveAddressVsAverage = currentActiveAddressVsAverage;
        this.currentTransactionCount = currentTransactionCount;
        this.averageTransactionCount = averageTransactionCount;
        this.currentTransactionCountVsAverage = currentTransactionCountVsAverage;
        this.currentMarketCapUsd = currentMarketCapUsd;
        this.averageMarketCapUsd = averageMarketCapUsd;
        this.currentMarketCapVsAverage = currentMarketCapVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSummary(
            Instant windowStartTime,
            Integer sampleCount,
            BigDecimal currentActiveAddressCount,
            BigDecimal averageActiveAddressCount,
            BigDecimal currentActiveAddressVsAverage,
            BigDecimal currentTransactionCount,
            BigDecimal averageTransactionCount,
            BigDecimal currentTransactionCountVsAverage,
            BigDecimal currentMarketCapUsd,
            BigDecimal averageMarketCapUsd,
            BigDecimal currentMarketCapVsAverage,
            String sourceDataVersion
    ) {
        this.windowStartTime = windowStartTime;
        this.sampleCount = sampleCount;
        this.currentActiveAddressCount = currentActiveAddressCount;
        this.averageActiveAddressCount = averageActiveAddressCount;
        this.currentActiveAddressVsAverage = currentActiveAddressVsAverage;
        this.currentTransactionCount = currentTransactionCount;
        this.averageTransactionCount = averageTransactionCount;
        this.currentTransactionCountVsAverage = currentTransactionCountVsAverage;
        this.currentMarketCapUsd = currentMarketCapUsd;
        this.averageMarketCapUsd = averageMarketCapUsd;
        this.currentMarketCapVsAverage = currentMarketCapVsAverage;
        this.sourceDataVersion = sourceDataVersion;
    }
}

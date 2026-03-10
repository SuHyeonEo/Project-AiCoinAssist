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
        name = "onchain_fact_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_onchain_fact_snapshot_symbol_snapshot_time",
                        columnNames = {"symbol", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_onchain_fact_snapshot_symbol_snapshot_time",
                        columnList = "symbol, snapshot_time"
                ),
                @Index(
                        name = "idx_onchain_fact_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OnchainFactSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String assetCode;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false)
    private Instant activeAddressSourceEventTime;

    @Column(nullable = false)
    private Instant transactionCountSourceEventTime;

    @Column(nullable = false)
    private Instant marketCapSourceEventTime;

    @Column(nullable = false, length = 250)
    private String sourceDataVersion;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal activeAddressCount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal transactionCount;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal marketCapUsd;

    @Builder
    public OnchainFactSnapshotEntity(
            String symbol,
            String assetCode,
            Instant snapshotTime,
            Instant activeAddressSourceEventTime,
            Instant transactionCountSourceEventTime,
            Instant marketCapSourceEventTime,
            String sourceDataVersion,
            BigDecimal activeAddressCount,
            BigDecimal transactionCount,
            BigDecimal marketCapUsd
    ) {
        this.symbol = symbol;
        this.assetCode = assetCode;
        this.snapshotTime = snapshotTime;
        this.activeAddressSourceEventTime = activeAddressSourceEventTime;
        this.transactionCountSourceEventTime = transactionCountSourceEventTime;
        this.marketCapSourceEventTime = marketCapSourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.activeAddressCount = activeAddressCount;
        this.transactionCount = transactionCount;
        this.marketCapUsd = marketCapUsd;
    }

    public void refreshFromSnapshot(
            Instant activeAddressSourceEventTime,
            Instant transactionCountSourceEventTime,
            Instant marketCapSourceEventTime,
            String sourceDataVersion,
            BigDecimal activeAddressCount,
            BigDecimal transactionCount,
            BigDecimal marketCapUsd
    ) {
        this.activeAddressSourceEventTime = activeAddressSourceEventTime;
        this.transactionCountSourceEventTime = transactionCountSourceEventTime;
        this.marketCapSourceEventTime = marketCapSourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.activeAddressCount = activeAddressCount;
        this.transactionCount = transactionCount;
        this.marketCapUsd = marketCapUsd;
    }
}

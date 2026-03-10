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
        name = "market_external_context_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_external_context_snapshot_symbol_snapshot_time",
                        columnNames = {"symbol", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_external_context_snapshot_symbol_snapshot_time",
                        columnList = "symbol, snapshot_time"
                ),
                @Index(
                        name = "idx_market_external_context_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketExternalContextSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column
    private Instant derivativeSnapshotTime;

    @Column
    private Instant macroSnapshotTime;

    @Column
    private Instant sentimentSnapshotTime;

    @Column
    private Instant onchainSnapshotTime;

    @Column(nullable = false, length = 500)
    private String sourceDataVersion;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal compositeRiskScore;

    @Column(length = 20)
    private String dominantDirection;

    @Column(length = 20)
    private String highestSeverity;

    @Column(nullable = false)
    private Integer supportiveSignalCount;

    @Column(nullable = false)
    private Integer cautionarySignalCount;

    @Column(nullable = false)
    private Integer headwindSignalCount;

    @Column(length = 20)
    private String primarySignalCategory;

    @Column(length = 120)
    private String primarySignalTitle;

    @Column(columnDefinition = "TEXT")
    private String primarySignalDetail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String regimeSignalsPayload;

    @Builder
    public MarketExternalContextSnapshotEntity(
            String symbol,
            Instant snapshotTime,
            Instant derivativeSnapshotTime,
            Instant macroSnapshotTime,
            Instant sentimentSnapshotTime,
            Instant onchainSnapshotTime,
            String sourceDataVersion,
            BigDecimal compositeRiskScore,
            String dominantDirection,
            String highestSeverity,
            Integer supportiveSignalCount,
            Integer cautionarySignalCount,
            Integer headwindSignalCount,
            String primarySignalCategory,
            String primarySignalTitle,
            String primarySignalDetail,
            String regimeSignalsPayload
    ) {
        this.symbol = symbol;
        this.snapshotTime = snapshotTime;
        this.derivativeSnapshotTime = derivativeSnapshotTime;
        this.macroSnapshotTime = macroSnapshotTime;
        this.sentimentSnapshotTime = sentimentSnapshotTime;
        this.onchainSnapshotTime = onchainSnapshotTime;
        this.sourceDataVersion = sourceDataVersion;
        this.compositeRiskScore = compositeRiskScore;
        this.dominantDirection = dominantDirection;
        this.highestSeverity = highestSeverity;
        this.supportiveSignalCount = supportiveSignalCount;
        this.cautionarySignalCount = cautionarySignalCount;
        this.headwindSignalCount = headwindSignalCount;
        this.primarySignalCategory = primarySignalCategory;
        this.primarySignalTitle = primarySignalTitle;
        this.primarySignalDetail = primarySignalDetail;
        this.regimeSignalsPayload = regimeSignalsPayload;
    }

    public void refreshFromSnapshot(
            Instant derivativeSnapshotTime,
            Instant macroSnapshotTime,
            Instant sentimentSnapshotTime,
            Instant onchainSnapshotTime,
            String sourceDataVersion,
            BigDecimal compositeRiskScore,
            String dominantDirection,
            String highestSeverity,
            Integer supportiveSignalCount,
            Integer cautionarySignalCount,
            Integer headwindSignalCount,
            String primarySignalCategory,
            String primarySignalTitle,
            String primarySignalDetail,
            String regimeSignalsPayload
    ) {
        this.derivativeSnapshotTime = derivativeSnapshotTime;
        this.macroSnapshotTime = macroSnapshotTime;
        this.sentimentSnapshotTime = sentimentSnapshotTime;
        this.onchainSnapshotTime = onchainSnapshotTime;
        this.sourceDataVersion = sourceDataVersion;
        this.compositeRiskScore = compositeRiskScore;
        this.dominantDirection = dominantDirection;
        this.highestSeverity = highestSeverity;
        this.supportiveSignalCount = supportiveSignalCount;
        this.cautionarySignalCount = cautionarySignalCount;
        this.headwindSignalCount = headwindSignalCount;
        this.primarySignalCategory = primarySignalCategory;
        this.primarySignalTitle = primarySignalTitle;
        this.primarySignalDetail = primarySignalDetail;
        this.regimeSignalsPayload = regimeSignalsPayload;
    }
}

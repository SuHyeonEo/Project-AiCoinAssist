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
        name = "market_candidate_level_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_candidate_level_snapshot_symbol_interval_level_snapshot",
                        columnNames = {"symbol", "interval_value", "snapshot_time", "level_type", "level_label"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_candidate_level_snapshot_symbol_interval_snapshot",
                        columnList = "symbol, interval_value, snapshot_time"
                ),
                @Index(
                        name = "idx_market_candidate_level_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketCandidateLevelSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String intervalValue;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false, length = 20)
    private String levelType;

    @Column(nullable = false, length = 20)
    private String levelLabel;

    @Column(nullable = false, length = 30)
    private String sourceType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal levelPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal distanceFromCurrent;

    @Column(precision = 19, scale = 8)
    private BigDecimal strengthScore;

    @Column(nullable = false, length = 200)
    private String rationale;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String triggerFactsPayload;

    @Column(nullable = false, length = 300)
    private String sourceDataVersion;

    @Builder
    public MarketCandidateLevelSnapshotEntity(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            String levelType,
            String levelLabel,
            String sourceType,
            BigDecimal currentPrice,
            BigDecimal levelPrice,
            BigDecimal distanceFromCurrent,
            BigDecimal strengthScore,
            String rationale,
            String triggerFactsPayload,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.snapshotTime = snapshotTime;
        this.levelType = levelType;
        this.levelLabel = levelLabel;
        this.sourceType = sourceType;
        this.currentPrice = currentPrice;
        this.levelPrice = levelPrice;
        this.distanceFromCurrent = distanceFromCurrent;
        this.strengthScore = strengthScore;
        this.rationale = rationale;
        this.triggerFactsPayload = triggerFactsPayload;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSnapshot(
            BigDecimal currentPrice,
            BigDecimal levelPrice,
            BigDecimal distanceFromCurrent,
            BigDecimal strengthScore,
            String rationale,
            String triggerFactsPayload,
            String sourceDataVersion
    ) {
        this.currentPrice = currentPrice;
        this.levelPrice = levelPrice;
        this.distanceFromCurrent = distanceFromCurrent;
        this.strengthScore = strengthScore;
        this.rationale = rationale;
        this.triggerFactsPayload = triggerFactsPayload;
        this.sourceDataVersion = sourceDataVersion;
    }
}

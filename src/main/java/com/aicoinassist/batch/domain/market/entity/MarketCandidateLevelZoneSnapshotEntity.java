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
        name = "market_candidate_level_zone_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_candidate_level_zone_snapshot_symbol_interval_zone_rank",
                        columnNames = {"symbol", "interval_value", "snapshot_time", "zone_type", "zone_rank"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_candidate_level_zone_snapshot_symbol_interval_snapshot",
                        columnList = "symbol, interval_value, snapshot_time"
                ),
                @Index(
                        name = "idx_market_candidate_level_zone_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketCandidateLevelZoneSnapshotEntity {

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
    private String zoneType;

    @Column(nullable = false)
    private Integer zoneRank;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal representativePrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal zoneLow;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal zoneHigh;

    @Column(precision = 19, scale = 8)
    private BigDecimal distanceFromCurrent;

    @Column(precision = 19, scale = 8)
    private BigDecimal zoneStrengthScore;

    @Column(nullable = false, length = 20)
    private String strongestLevelLabel;

    @Column(nullable = false, length = 30)
    private String strongestSourceType;

    @Column(nullable = false)
    private Integer levelCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String includedLevelLabelsPayload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String includedSourceTypesPayload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String triggerFactsPayload;

    @Column(nullable = false, length = 500)
    private String sourceDataVersion;

    @Builder
    public MarketCandidateLevelZoneSnapshotEntity(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            String zoneType,
            Integer zoneRank,
            BigDecimal currentPrice,
            BigDecimal representativePrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh,
            BigDecimal distanceFromCurrent,
            BigDecimal zoneStrengthScore,
            String strongestLevelLabel,
            String strongestSourceType,
            Integer levelCount,
            String includedLevelLabelsPayload,
            String includedSourceTypesPayload,
            String triggerFactsPayload,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.snapshotTime = snapshotTime;
        this.zoneType = zoneType;
        this.zoneRank = zoneRank;
        this.currentPrice = currentPrice;
        this.representativePrice = representativePrice;
        this.zoneLow = zoneLow;
        this.zoneHigh = zoneHigh;
        this.distanceFromCurrent = distanceFromCurrent;
        this.zoneStrengthScore = zoneStrengthScore;
        this.strongestLevelLabel = strongestLevelLabel;
        this.strongestSourceType = strongestSourceType;
        this.levelCount = levelCount;
        this.includedLevelLabelsPayload = includedLevelLabelsPayload;
        this.includedSourceTypesPayload = includedSourceTypesPayload;
        this.triggerFactsPayload = triggerFactsPayload;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSnapshot(
            BigDecimal currentPrice,
            BigDecimal representativePrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh,
            BigDecimal distanceFromCurrent,
            BigDecimal zoneStrengthScore,
            String strongestLevelLabel,
            String strongestSourceType,
            Integer levelCount,
            String includedLevelLabelsPayload,
            String includedSourceTypesPayload,
            String triggerFactsPayload,
            String sourceDataVersion
    ) {
        this.currentPrice = currentPrice;
        this.representativePrice = representativePrice;
        this.zoneLow = zoneLow;
        this.zoneHigh = zoneHigh;
        this.distanceFromCurrent = distanceFromCurrent;
        this.zoneStrengthScore = zoneStrengthScore;
        this.strongestLevelLabel = strongestLevelLabel;
        this.strongestSourceType = strongestSourceType;
        this.levelCount = levelCount;
        this.includedLevelLabelsPayload = includedLevelLabelsPayload;
        this.includedSourceTypesPayload = includedSourceTypesPayload;
        this.triggerFactsPayload = triggerFactsPayload;
        this.sourceDataVersion = sourceDataVersion;
    }
}

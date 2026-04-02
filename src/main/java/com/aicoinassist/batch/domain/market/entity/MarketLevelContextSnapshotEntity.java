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
        name = "market_level_context_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_level_context_snapshot_symbol_interval_snapshot_time",
                        columnNames = {"symbol", "interval_value", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_level_context_snapshot_symbol_interval_snapshot_time",
                        columnList = "symbol, interval_value, snapshot_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketLevelContextSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String intervalValue;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentPrice;

    private Integer supportZoneRank;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportRepresentativePrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportZoneLow;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportZoneHigh;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportDistanceToZone;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportZoneStrength;

    @Column(length = 20)
    private String supportInteractionType;

    private Integer supportRecentTestCount;

    private Integer supportRecentRejectionCount;

    private Integer supportRecentBreakCount;

    @Column(precision = 19, scale = 8)
    private BigDecimal supportBreakRisk;

    private Integer resistanceZoneRank;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceRepresentativePrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceZoneLow;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceZoneHigh;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceDistanceToZone;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceZoneStrength;

    @Column(length = 20)
    private String resistanceInteractionType;

    private Integer resistanceRecentTestCount;

    private Integer resistanceRecentRejectionCount;

    private Integer resistanceRecentBreakCount;

    @Column(precision = 19, scale = 8)
    private BigDecimal resistanceBreakRisk;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceDataVersion;

    @Builder
    public MarketLevelContextSnapshotEntity(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            BigDecimal currentPrice,
            Integer supportZoneRank,
            BigDecimal supportRepresentativePrice,
            BigDecimal supportZoneLow,
            BigDecimal supportZoneHigh,
            BigDecimal supportDistanceToZone,
            BigDecimal supportZoneStrength,
            String supportInteractionType,
            Integer supportRecentTestCount,
            Integer supportRecentRejectionCount,
            Integer supportRecentBreakCount,
            BigDecimal supportBreakRisk,
            Integer resistanceZoneRank,
            BigDecimal resistanceRepresentativePrice,
            BigDecimal resistanceZoneLow,
            BigDecimal resistanceZoneHigh,
            BigDecimal resistanceDistanceToZone,
            BigDecimal resistanceZoneStrength,
            String resistanceInteractionType,
            Integer resistanceRecentTestCount,
            Integer resistanceRecentRejectionCount,
            Integer resistanceRecentBreakCount,
            BigDecimal resistanceBreakRisk,
            String sourceDataVersion
    ) {
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.snapshotTime = snapshotTime;
        this.currentPrice = currentPrice;
        this.supportZoneRank = supportZoneRank;
        this.supportRepresentativePrice = supportRepresentativePrice;
        this.supportZoneLow = supportZoneLow;
        this.supportZoneHigh = supportZoneHigh;
        this.supportDistanceToZone = supportDistanceToZone;
        this.supportZoneStrength = supportZoneStrength;
        this.supportInteractionType = supportInteractionType;
        this.supportRecentTestCount = supportRecentTestCount;
        this.supportRecentRejectionCount = supportRecentRejectionCount;
        this.supportRecentBreakCount = supportRecentBreakCount;
        this.supportBreakRisk = supportBreakRisk;
        this.resistanceZoneRank = resistanceZoneRank;
        this.resistanceRepresentativePrice = resistanceRepresentativePrice;
        this.resistanceZoneLow = resistanceZoneLow;
        this.resistanceZoneHigh = resistanceZoneHigh;
        this.resistanceDistanceToZone = resistanceDistanceToZone;
        this.resistanceZoneStrength = resistanceZoneStrength;
        this.resistanceInteractionType = resistanceInteractionType;
        this.resistanceRecentTestCount = resistanceRecentTestCount;
        this.resistanceRecentRejectionCount = resistanceRecentRejectionCount;
        this.resistanceRecentBreakCount = resistanceRecentBreakCount;
        this.resistanceBreakRisk = resistanceBreakRisk;
        this.sourceDataVersion = sourceDataVersion;
    }

    public void refreshFromSnapshot(
            BigDecimal currentPrice,
            Integer supportZoneRank,
            BigDecimal supportRepresentativePrice,
            BigDecimal supportZoneLow,
            BigDecimal supportZoneHigh,
            BigDecimal supportDistanceToZone,
            BigDecimal supportZoneStrength,
            String supportInteractionType,
            Integer supportRecentTestCount,
            Integer supportRecentRejectionCount,
            Integer supportRecentBreakCount,
            BigDecimal supportBreakRisk,
            Integer resistanceZoneRank,
            BigDecimal resistanceRepresentativePrice,
            BigDecimal resistanceZoneLow,
            BigDecimal resistanceZoneHigh,
            BigDecimal resistanceDistanceToZone,
            BigDecimal resistanceZoneStrength,
            String resistanceInteractionType,
            Integer resistanceRecentTestCount,
            Integer resistanceRecentRejectionCount,
            Integer resistanceRecentBreakCount,
            BigDecimal resistanceBreakRisk,
            String sourceDataVersion
    ) {
        this.currentPrice = currentPrice;
        this.supportZoneRank = supportZoneRank;
        this.supportRepresentativePrice = supportRepresentativePrice;
        this.supportZoneLow = supportZoneLow;
        this.supportZoneHigh = supportZoneHigh;
        this.supportDistanceToZone = supportDistanceToZone;
        this.supportZoneStrength = supportZoneStrength;
        this.supportInteractionType = supportInteractionType;
        this.supportRecentTestCount = supportRecentTestCount;
        this.supportRecentRejectionCount = supportRecentRejectionCount;
        this.supportRecentBreakCount = supportRecentBreakCount;
        this.supportBreakRisk = supportBreakRisk;
        this.resistanceZoneRank = resistanceZoneRank;
        this.resistanceRepresentativePrice = resistanceRepresentativePrice;
        this.resistanceZoneLow = resistanceZoneLow;
        this.resistanceZoneHigh = resistanceZoneHigh;
        this.resistanceDistanceToZone = resistanceDistanceToZone;
        this.resistanceZoneStrength = resistanceZoneStrength;
        this.resistanceInteractionType = resistanceInteractionType;
        this.resistanceRecentTestCount = resistanceRecentTestCount;
        this.resistanceRecentRejectionCount = resistanceRecentRejectionCount;
        this.resistanceRecentBreakCount = resistanceRecentBreakCount;
        this.resistanceBreakRisk = resistanceBreakRisk;
        this.sourceDataVersion = sourceDataVersion;
    }
}

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
        name = "market_context_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_context_snapshot_symbol_snapshot_time",
                        columnNames = {"symbol", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_context_snapshot_symbol_snapshot_time",
                        columnList = "symbol, snapshot_time"
                ),
                @Index(
                        name = "idx_market_context_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketContextSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false)
    private Instant openInterestSourceEventTime;

    @Column(nullable = false)
    private Instant premiumIndexSourceEventTime;

    @Column(nullable = false, length = 200)
    private String sourceDataVersion;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal openInterest;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal markPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal indexPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal lastFundingRate;

    @Column(nullable = false)
    private Instant nextFundingTime;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal markIndexBasisRate;

    @Builder
    public MarketContextSnapshotEntity(
            String symbol,
            Instant snapshotTime,
            Instant openInterestSourceEventTime,
            Instant premiumIndexSourceEventTime,
            String sourceDataVersion,
            BigDecimal openInterest,
            BigDecimal markPrice,
            BigDecimal indexPrice,
            BigDecimal lastFundingRate,
            Instant nextFundingTime,
            BigDecimal markIndexBasisRate
    ) {
        this.symbol = symbol;
        this.snapshotTime = snapshotTime;
        this.openInterestSourceEventTime = openInterestSourceEventTime;
        this.premiumIndexSourceEventTime = premiumIndexSourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.openInterest = openInterest;
        this.markPrice = markPrice;
        this.indexPrice = indexPrice;
        this.lastFundingRate = lastFundingRate;
        this.nextFundingTime = nextFundingTime;
        this.markIndexBasisRate = markIndexBasisRate;
    }

    public void refreshFromSnapshot(
            Instant openInterestSourceEventTime,
            Instant premiumIndexSourceEventTime,
            String sourceDataVersion,
            BigDecimal openInterest,
            BigDecimal markPrice,
            BigDecimal indexPrice,
            BigDecimal lastFundingRate,
            Instant nextFundingTime,
            BigDecimal markIndexBasisRate
    ) {
        this.openInterestSourceEventTime = openInterestSourceEventTime;
        this.premiumIndexSourceEventTime = premiumIndexSourceEventTime;
        this.sourceDataVersion = sourceDataVersion;
        this.openInterest = openInterest;
        this.markPrice = markPrice;
        this.indexPrice = indexPrice;
        this.lastFundingRate = lastFundingRate;
        this.nextFundingTime = nextFundingTime;
        this.markIndexBasisRate = markIndexBasisRate;
    }
}

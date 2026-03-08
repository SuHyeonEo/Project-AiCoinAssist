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
        name = "market_indicator_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_indicator_snapshot_symbol_interval_snapshot_time",
                        columnNames = {"symbol", "interval_value", "snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_indicator_snapshot_symbol_interval_snapshot_time",
                        columnList = "symbol, interval_value, snapshot_time"
                ),
                @Index(
                        name = "idx_market_indicator_snapshot_price_source_event_time",
                        columnList = "price_source_event_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketIndicatorSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String intervalValue;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false)
    private Instant latestCandleOpenTime;

    @Column(nullable = false)
    private Instant priceSourceEventTime;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal ma20;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal ma60;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal ma120;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rsi14;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal macdLine;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal macdSignalLine;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal macdHistogram;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal atr14;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal bollingerUpperBand;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal bollingerMiddleBand;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal bollingerLowerBand;

    @Builder
    public MarketIndicatorSnapshotEntity(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            Instant latestCandleOpenTime,
            Instant priceSourceEventTime,
            BigDecimal currentPrice,
            BigDecimal ma20,
            BigDecimal ma60,
            BigDecimal ma120,
            BigDecimal rsi14,
            BigDecimal macdLine,
            BigDecimal macdSignalLine,
            BigDecimal macdHistogram,
            BigDecimal atr14,
            BigDecimal bollingerUpperBand,
            BigDecimal bollingerMiddleBand,
            BigDecimal bollingerLowerBand
    ) {
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.snapshotTime = snapshotTime;
        this.latestCandleOpenTime = latestCandleOpenTime;
        this.priceSourceEventTime = priceSourceEventTime;
        this.currentPrice = currentPrice;
        this.ma20 = ma20;
        this.ma60 = ma60;
        this.ma120 = ma120;
        this.rsi14 = rsi14;
        this.macdLine = macdLine;
        this.macdSignalLine = macdSignalLine;
        this.macdHistogram = macdHistogram;
        this.atr14 = atr14;
        this.bollingerUpperBand = bollingerUpperBand;
        this.bollingerMiddleBand = bollingerMiddleBand;
        this.bollingerLowerBand = bollingerLowerBand;
    }

    public void refreshFromSnapshot(
            Instant latestCandleOpenTime,
            Instant priceSourceEventTime,
            BigDecimal currentPrice,
            BigDecimal ma20,
            BigDecimal ma60,
            BigDecimal ma120,
            BigDecimal rsi14,
            BigDecimal macdLine,
            BigDecimal macdSignalLine,
            BigDecimal macdHistogram,
            BigDecimal atr14,
            BigDecimal bollingerUpperBand,
            BigDecimal bollingerMiddleBand,
            BigDecimal bollingerLowerBand
    ) {
        this.latestCandleOpenTime = latestCandleOpenTime;
        this.priceSourceEventTime = priceSourceEventTime;
        this.currentPrice = currentPrice;
        this.ma20 = ma20;
        this.ma60 = ma60;
        this.ma120 = ma120;
        this.rsi14 = rsi14;
        this.macdLine = macdLine;
        this.macdSignalLine = macdSignalLine;
        this.macdHistogram = macdHistogram;
        this.atr14 = atr14;
        this.bollingerUpperBand = bollingerUpperBand;
        this.bollingerMiddleBand = bollingerMiddleBand;
        this.bollingerLowerBand = bollingerLowerBand;
    }
}

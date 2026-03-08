package com.aicoinassist.batch.domain.market.entity;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
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
        name = "market_candle_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_candle_raw_source_symbol_interval_open_time",
                        columnNames = {"source", "symbol", "interval_value", "open_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_candle_raw_symbol_interval_collected_time",
                        columnList = "symbol, interval_value, collected_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketCandleRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String intervalValue;

    @Column
    private Instant openTime;

    @Column
    private Instant closeTime;

    @Column(precision = 19, scale = 8)
    private BigDecimal openPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal highPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal lowPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal closePrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal volume;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(length = 500)
    private String validationDetails;

    @Lob
    @Column(nullable = false)
    private String rawPayload;

    @Builder
    public MarketCandleRawEntity(
            String source,
            String symbol,
            String intervalValue,
            Instant openTime,
            Instant closeTime,
            BigDecimal openPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal closePrice,
            BigDecimal volume,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            String rawPayload
    ) {
        this.source = source;
        this.symbol = symbol;
        this.intervalValue = intervalValue;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            Instant closeTime,
            BigDecimal openPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal closePrice,
            BigDecimal volume,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            String rawPayload
    ) {
        this.closeTime = closeTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.rawPayload = rawPayload;
    }
}

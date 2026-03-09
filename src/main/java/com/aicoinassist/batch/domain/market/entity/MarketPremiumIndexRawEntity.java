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
        name = "market_premium_index_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_premium_index_raw_source_symbol_source_event_time",
                        columnNames = {"source", "symbol", "source_event_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_market_premium_index_raw_symbol_collected_time",
                        columnList = "symbol, collected_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketPremiumIndexRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column
    private Instant sourceEventTime;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(length = 500)
    private String validationDetails;

    @Column(precision = 19, scale = 8)
    private BigDecimal markPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal indexPrice;

    @Column(precision = 19, scale = 8)
    private BigDecimal lastFundingRate;

    @Column
    private Instant nextFundingTime;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Builder
    public MarketPremiumIndexRawEntity(
            String source,
            String symbol,
            Instant sourceEventTime,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal markPrice,
            BigDecimal indexPrice,
            BigDecimal lastFundingRate,
            Instant nextFundingTime,
            String rawPayload
    ) {
        this.source = source;
        this.symbol = symbol;
        this.sourceEventTime = sourceEventTime;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.markPrice = markPrice;
        this.indexPrice = indexPrice;
        this.lastFundingRate = lastFundingRate;
        this.nextFundingTime = nextFundingTime;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            BigDecimal markPrice,
            BigDecimal indexPrice,
            BigDecimal lastFundingRate,
            Instant nextFundingTime,
            String rawPayload
    ) {
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.markPrice = markPrice;
        this.indexPrice = indexPrice;
        this.lastFundingRate = lastFundingRate;
        this.nextFundingTime = nextFundingTime;
        this.rawPayload = rawPayload;
    }
}

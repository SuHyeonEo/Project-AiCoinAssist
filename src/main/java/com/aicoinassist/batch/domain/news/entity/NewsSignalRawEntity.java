package com.aicoinassist.batch.domain.news.entity;

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

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "news_signal_raw",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_news_signal_raw_source_asset_article_url",
                        columnNames = {"source", "asset_code", "article_url"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_news_signal_raw_asset_seen_time",
                        columnList = "asset_code, seen_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsSignalRawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, length = 20)
    private String assetCode;

    @Column(nullable = false, length = 200)
    private String queryText;

    @Column
    private Instant seenTime;

    @Column(length = 512)
    private String articleUrl;

    @Column(length = 512)
    private String mobileUrl;

    @Column(length = 500)
    private String title;

    @Column(length = 100)
    private String domain;

    @Column(length = 20)
    private String sourceLanguage;

    @Column(length = 20)
    private String sourceCountry;

    @Column(length = 512)
    private String socialImageUrl;

    @Column(nullable = false)
    private Instant collectedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RawDataValidationStatus validationStatus;

    @Column(columnDefinition = "TEXT")
    private String validationDetails;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Builder
    public NewsSignalRawEntity(
            String source,
            String assetCode,
            String queryText,
            Instant seenTime,
            String articleUrl,
            String mobileUrl,
            String title,
            String domain,
            String sourceLanguage,
            String sourceCountry,
            String socialImageUrl,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            String rawPayload
    ) {
        this.source = source;
        this.assetCode = assetCode;
        this.queryText = queryText;
        this.seenTime = seenTime;
        this.articleUrl = articleUrl;
        this.mobileUrl = mobileUrl;
        this.title = title;
        this.domain = domain;
        this.sourceLanguage = sourceLanguage;
        this.sourceCountry = sourceCountry;
        this.socialImageUrl = socialImageUrl;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.rawPayload = rawPayload;
    }

    public void refreshFromIngestion(
            Instant seenTime,
            String mobileUrl,
            String title,
            String domain,
            String sourceLanguage,
            String sourceCountry,
            String socialImageUrl,
            Instant collectedTime,
            RawDataValidationStatus validationStatus,
            String validationDetails,
            String rawPayload
    ) {
        this.seenTime = seenTime;
        this.mobileUrl = mobileUrl;
        this.title = title;
        this.domain = domain;
        this.sourceLanguage = sourceLanguage;
        this.sourceCountry = sourceCountry;
        this.socialImageUrl = socialImageUrl;
        this.collectedTime = collectedTime;
        this.validationStatus = validationStatus;
        this.validationDetails = validationDetails;
        this.rawPayload = rawPayload;
    }
}

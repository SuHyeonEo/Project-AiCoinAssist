package com.aicoinassist.batch.domain.news.entity;

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
        name = "news_signal_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_news_signal_snapshot_symbol_article_url",
                        columnNames = {"symbol", "article_url"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_news_signal_snapshot_symbol_snapshot_time",
                        columnList = "symbol, snapshot_time"
                ),
                @Index(
                        name = "idx_news_signal_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsSignalSnapshotEntity {

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
    private Instant seenTime;

    @Column(nullable = false, length = 200)
    private String sourceDataVersion;

    @Column(nullable = false, length = 512)
    private String articleUrl;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 100)
    private String domain;

    @Column(length = 20)
    private String sourceLanguage;

    @Column(length = 20)
    private String sourceCountry;

    @Column(nullable = false)
    private Integer titleKeywordHitCount;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal priorityScore;

    @Builder
    public NewsSignalSnapshotEntity(
            String symbol,
            String assetCode,
            Instant snapshotTime,
            Instant seenTime,
            String sourceDataVersion,
            String articleUrl,
            String title,
            String domain,
            String sourceLanguage,
            String sourceCountry,
            Integer titleKeywordHitCount,
            BigDecimal priorityScore
    ) {
        this.symbol = symbol;
        this.assetCode = assetCode;
        this.snapshotTime = snapshotTime;
        this.seenTime = seenTime;
        this.sourceDataVersion = sourceDataVersion;
        this.articleUrl = articleUrl;
        this.title = title;
        this.domain = domain;
        this.sourceLanguage = sourceLanguage;
        this.sourceCountry = sourceCountry;
        this.titleKeywordHitCount = titleKeywordHitCount;
        this.priorityScore = priorityScore;
    }

    public void refreshFromSnapshot(
            Instant snapshotTime,
            Instant seenTime,
            String sourceDataVersion,
            String title,
            String domain,
            String sourceLanguage,
            String sourceCountry,
            Integer titleKeywordHitCount,
            BigDecimal priorityScore
    ) {
        this.snapshotTime = snapshotTime;
        this.seenTime = seenTime;
        this.sourceDataVersion = sourceDataVersion;
        this.title = title;
        this.domain = domain;
        this.sourceLanguage = sourceLanguage;
        this.sourceCountry = sourceCountry;
        this.titleKeywordHitCount = titleKeywordHitCount;
        this.priorityScore = priorityScore;
    }
}

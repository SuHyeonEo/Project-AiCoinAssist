package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.news.dto.NewsSignalSnapshot;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalRawRepository;
import com.aicoinassist.batch.domain.news.support.NewsAssetKeywordSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NewsSignalSnapshotService {

    private final NewsSignalRawRepository newsSignalRawRepository;
    private final NewsAssetKeywordSupport newsAssetKeywordSupport;

    public List<NewsSignalSnapshot> createLatestSnapshots(AssetType assetType) {
        String assetCode = newsAssetKeywordSupport.assetCode(assetType);
        List<String> keywords = newsAssetKeywordSupport.keywords(assetType);

        return newsSignalRawRepository
                .findTop20ByAssetCodeAndValidationStatusOrderBySeenTimeDescCollectedTimeDescIdDesc(
                        assetCode,
                        RawDataValidationStatus.VALID
                )
                .stream()
                .filter(entity -> entity.getArticleUrl() != null && !entity.getArticleUrl().isBlank())
                .filter(entity -> entity.getSeenTime() != null)
                .map(entity -> toSnapshot(assetType, keywords, entity))
                .toList();
    }

    private NewsSignalSnapshot toSnapshot(
            AssetType assetType,
            List<String> keywords,
            NewsSignalRawEntity entity
    ) {
        int keywordHitCount = keywordHitCount(entity.getTitle(), keywords);
        BigDecimal priorityScore = priorityScore(keywordHitCount);

        return new NewsSignalSnapshot(
                assetType.name(),
                entity.getAssetCode(),
                entity.getSeenTime(),
                entity.getSeenTime(),
                buildSourceDataVersion(entity),
                entity.getArticleUrl(),
                entity.getTitle(),
                entity.getDomain(),
                entity.getSourceLanguage(),
                entity.getSourceCountry(),
                keywordHitCount,
                priorityScore
        );
    }

    private int keywordHitCount(String title, List<String> keywords) {
        if (title == null || title.isBlank()) {
            return 0;
        }

        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        int hitCount = 0;
        for (String keyword : keywords) {
            if (normalizedTitle.contains(keyword)) {
                hitCount++;
            }
        }
        return hitCount;
    }

    private BigDecimal priorityScore(int keywordHitCount) {
        BigDecimal baseScore = new BigDecimal("0.2500");
        BigDecimal hitScore = new BigDecimal("0.2500").multiply(BigDecimal.valueOf(keywordHitCount));
        BigDecimal priorityScore = baseScore.add(hitScore);
        if (priorityScore.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        }
        return priorityScore.setScale(4, RoundingMode.HALF_UP);
    }

    private String buildSourceDataVersion(NewsSignalRawEntity entity) {
        return "seenTime=" + entity.getSeenTime()
                + ";urlHash=" + articleUrlHash(entity.getArticleUrl());
    }

    private String articleUrlHash(String articleUrl) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(articleUrl.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm must be available.", exception);
        }
    }
}

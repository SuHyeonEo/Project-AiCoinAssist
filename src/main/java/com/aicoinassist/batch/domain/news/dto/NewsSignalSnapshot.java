package com.aicoinassist.batch.domain.news.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record NewsSignalSnapshot(
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
}

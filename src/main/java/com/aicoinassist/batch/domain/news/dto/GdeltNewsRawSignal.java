package com.aicoinassist.batch.domain.news.dto;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;

import java.time.Instant;

public record GdeltNewsRawSignal(
        String assetCode,
        String queryText,
        Instant seenTime,
        RawDataValidationResult validation,
        String articleUrl,
        String mobileUrl,
        String title,
        String domain,
        String sourceLanguage,
        String sourceCountry,
        String socialImageUrl,
        String rawPayload
) {
}

package com.aicoinassist.batch.domain.sentiment.dto;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;

import java.math.BigDecimal;
import java.time.Instant;

public record FearGreedRawSnapshot(
        SentimentMetricType metricType,
        Instant sourceEventTime,
        RawDataValidationResult validation,
        BigDecimal indexValue,
        String classification,
        Long timeUntilUpdateSeconds,
        String rawPayload
) {
}

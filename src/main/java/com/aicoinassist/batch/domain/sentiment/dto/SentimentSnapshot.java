package com.aicoinassist.batch.domain.sentiment.dto;

import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;

import java.math.BigDecimal;
import java.time.Instant;

public record SentimentSnapshot(
        SentimentMetricType metricType,
        Instant snapshotTime,
        Instant sourceEventTime,
        String sourceDataVersion,
        BigDecimal indexValue,
        String classification,
        Long timeUntilUpdateSeconds,
        Instant previousSnapshotTime,
        BigDecimal previousIndexValue,
        BigDecimal valueChange,
        BigDecimal valueChangeRate,
        Boolean classificationChanged
) {
}

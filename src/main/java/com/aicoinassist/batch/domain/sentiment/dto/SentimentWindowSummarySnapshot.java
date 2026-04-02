package com.aicoinassist.batch.domain.sentiment.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;

import java.math.BigDecimal;
import java.time.Instant;

public record SentimentWindowSummarySnapshot(
        SentimentMetricType metricType,
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal currentIndexValue,
        BigDecimal averageIndexValue,
        BigDecimal currentIndexVsAverage,
        String currentClassification,
        Integer greedSampleCount,
        Integer fearSampleCount,
        String sourceDataVersion
) {
}

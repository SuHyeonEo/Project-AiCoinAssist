package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisSentimentWindowSummary(
        MarketWindowType windowType,
        Instant windowStartTime,
        Instant windowEndTime,
        Integer sampleCount,
        BigDecimal averageIndexValue,
        BigDecimal currentIndexVsAverage,
        Integer greedSampleCount,
        Integer fearSampleCount
) {
}

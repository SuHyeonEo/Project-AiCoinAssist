package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referencePrice,
        BigDecimal priceChangeRate,
        BigDecimal rsiDelta,
        BigDecimal macdHistogramDelta,
        BigDecimal atrChangeRate
) {
}

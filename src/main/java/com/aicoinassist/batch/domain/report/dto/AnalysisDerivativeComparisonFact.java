package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisDerivativeComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referenceOpenInterest,
        BigDecimal openInterestChangeRate,
        BigDecimal fundingRateDelta,
        BigDecimal basisRateDelta
) {
}

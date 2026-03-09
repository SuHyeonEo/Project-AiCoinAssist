package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AnalysisDerivativeContext(
        Instant snapshotTime,
        Instant openInterestSourceEventTime,
        Instant premiumIndexSourceEventTime,
        String sourceDataVersion,
        BigDecimal openInterest,
        BigDecimal markPrice,
        BigDecimal indexPrice,
        BigDecimal lastFundingRate,
        Instant nextFundingTime,
        BigDecimal markIndexBasisRate,
        List<AnalysisDerivativeComparisonFact> comparisonFacts,
        List<AnalysisDerivativeWindowSummary> windowSummaries
) {
}

package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;

public record AnalysisMarketContextPayload(
        BigDecimal currentPrice,
        String currentTrendLabel,
        String volatilityLabel,
        String rangePositionLabel,
        String maPositionSummary,
        String momentumSummary,
        String comparisonSummary,
        String windowSummary,
        String derivativeContextSummary,
        String continuitySummary
) {
}

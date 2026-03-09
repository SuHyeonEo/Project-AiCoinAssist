package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;

public record AnalysisCurrentStatePayload(
        BigDecimal currentPrice,
        String trendLabel,
        String volatilityLabel,
        String rangePositionLabel,
        String maPositionSummary,
        String momentumSummary
) {
}

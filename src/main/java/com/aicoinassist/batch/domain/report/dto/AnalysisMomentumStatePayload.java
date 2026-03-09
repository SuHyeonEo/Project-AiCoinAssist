package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;

public record AnalysisMomentumStatePayload(
        BigDecimal rsi14,
        BigDecimal macdHistogram,
        String signalSummary
) {
}

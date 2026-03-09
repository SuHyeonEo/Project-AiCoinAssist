package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;

public record AnalysisMovingAveragePositionPayload(
        String movingAverageName,
        BigDecimal level,
        boolean priceAbove
) {
}

package com.aicoinassist.batch.domain.indicator.dto;

import java.math.BigDecimal;

public record MovingAverageResult(
        int period,
        BigDecimal value
) {
}
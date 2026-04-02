package com.aicoinassist.batch.domain.indicator.dto;

import java.math.BigDecimal;

public record RsiResult(
        int period,
        BigDecimal value
) {
}
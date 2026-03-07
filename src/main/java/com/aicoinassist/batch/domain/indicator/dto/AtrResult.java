package com.aicoinassist.batch.domain.indicator.dto;

import java.math.BigDecimal;

public record AtrResult(
        int period,
        BigDecimal value
) {
}
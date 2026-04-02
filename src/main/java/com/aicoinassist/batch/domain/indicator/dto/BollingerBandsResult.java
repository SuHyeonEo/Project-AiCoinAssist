package com.aicoinassist.batch.domain.indicator.dto;

import java.math.BigDecimal;

public record BollingerBandsResult(
        int period,
        BigDecimal upperBand,
        BigDecimal middleBand,
        BigDecimal lowerBand
) {
}
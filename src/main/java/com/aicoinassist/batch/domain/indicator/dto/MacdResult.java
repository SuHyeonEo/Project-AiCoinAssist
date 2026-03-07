package com.aicoinassist.batch.domain.indicator.dto;

import java.math.BigDecimal;

public record MacdResult(
        BigDecimal macdLine,
        BigDecimal signalLine,
        BigDecimal histogram
) {
}
package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;

public record AnalysisPriceLevel(
        String label,
        BigDecimal price,
        String rationale
) {
}

package com.aicoinassist.batch.domain.macro.dto;

import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FredMacroRawSnapshot(
        MacroMetricType metricType,
        String seriesId,
        String units,
        LocalDate observationDate,
        RawDataValidationResult validation,
        BigDecimal metricValue,
        String rawPayload
) {
}

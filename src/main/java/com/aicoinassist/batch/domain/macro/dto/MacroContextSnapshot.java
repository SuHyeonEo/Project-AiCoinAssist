package com.aicoinassist.batch.domain.macro.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MacroContextSnapshot(
        Instant snapshotTime,
        LocalDate dxyObservationDate,
        LocalDate us10yYieldObservationDate,
        LocalDate usdKrwObservationDate,
        String sourceDataVersion,
        BigDecimal dxyProxyValue,
        BigDecimal us10yYieldValue,
        BigDecimal usdKrwValue
) {
}

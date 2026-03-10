package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AnalysisMacroContext(
        Instant snapshotTime,
        String sourceDataVersion,
        LocalDate dxyObservationDate,
        LocalDate us10yYieldObservationDate,
        LocalDate usdKrwObservationDate,
        BigDecimal dxyProxyValue,
        BigDecimal us10yYieldValue,
        BigDecimal usdKrwValue,
        List<AnalysisMacroComparisonFact> comparisonFacts,
        List<AnalysisMacroHighlight> highlights
) {
}

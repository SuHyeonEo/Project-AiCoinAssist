package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AnalysisOnchainContext(
        Instant snapshotTime,
        Instant activeAddressSourceEventTime,
        Instant transactionCountSourceEventTime,
        Instant marketCapSourceEventTime,
        String sourceDataVersion,
        BigDecimal activeAddressCount,
        BigDecimal transactionCount,
        BigDecimal marketCapUsd,
        List<AnalysisOnchainComparisonFact> comparisonFacts,
        List<AnalysisOnchainHighlight> highlights
) {
}

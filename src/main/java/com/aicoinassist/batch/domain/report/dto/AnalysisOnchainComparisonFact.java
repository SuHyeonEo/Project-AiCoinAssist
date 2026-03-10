package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisOnchainComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referenceActiveAddressCount,
        BigDecimal activeAddressChangeRate,
        BigDecimal referenceTransactionCount,
        BigDecimal transactionCountChangeRate,
        BigDecimal referenceMarketCapUsd,
        BigDecimal marketCapChangeRate
) {
}

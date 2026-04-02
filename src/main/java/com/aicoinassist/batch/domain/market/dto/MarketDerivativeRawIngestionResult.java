package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;

import java.time.Instant;

public record MarketDerivativeRawIngestionResult(
        String symbol,
        Instant collectedTime,
        RawDataValidationStatus openInterestValidationStatus,
        RawDataValidationStatus premiumIndexValidationStatus
) {
}

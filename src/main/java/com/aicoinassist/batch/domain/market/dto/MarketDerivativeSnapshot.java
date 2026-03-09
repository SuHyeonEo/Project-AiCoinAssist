package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketDerivativeSnapshot(
        String symbol,
        Instant openInterestSourceEventTime,
        RawDataValidationResult openInterestValidation,
        BigDecimal openInterest,
        String openInterestRawPayload,
        Instant premiumIndexSourceEventTime,
        RawDataValidationResult premiumIndexValidation,
        BigDecimal markPrice,
        BigDecimal indexPrice,
        BigDecimal lastFundingRate,
        Instant nextFundingTime,
        String premiumIndexRawPayload
) {
}

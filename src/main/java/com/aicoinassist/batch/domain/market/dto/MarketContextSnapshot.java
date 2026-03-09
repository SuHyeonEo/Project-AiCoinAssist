package com.aicoinassist.batch.domain.market.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketContextSnapshot(
        String symbol,
        Instant snapshotTime,
        Instant openInterestSourceEventTime,
        Instant premiumIndexSourceEventTime,
        String sourceDataVersion,
        BigDecimal openInterest,
        BigDecimal markPrice,
        BigDecimal indexPrice,
        BigDecimal lastFundingRate,
        Instant nextFundingTime,
        BigDecimal markIndexBasisRate
) {
}

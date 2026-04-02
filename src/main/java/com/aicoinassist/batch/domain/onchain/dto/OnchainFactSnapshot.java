package com.aicoinassist.batch.domain.onchain.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OnchainFactSnapshot(
        String symbol,
        String assetCode,
        Instant snapshotTime,
        Instant activeAddressSourceEventTime,
        Instant transactionCountSourceEventTime,
        Instant marketCapSourceEventTime,
        String sourceDataVersion,
        BigDecimal activeAddressCount,
        BigDecimal transactionCount,
        BigDecimal marketCapUsd
) {
}

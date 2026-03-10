package com.aicoinassist.batch.domain.onchain.dto;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoinMetricsOnchainRawSnapshot(
        String assetCode,
        OnchainMetricType metricType,
        Instant sourceEventTime,
        RawDataValidationResult validation,
        BigDecimal metricValue,
        String rawPayload
) {
}

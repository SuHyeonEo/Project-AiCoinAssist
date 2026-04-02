package com.aicoinassist.batch.domain.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler.external-raw-ingestion")
public record ExternalRawIngestionProperties(
        boolean enabled,
        boolean marketPriceEnabled,
        Long marketPriceFixedDelayMs,
        Long marketPriceInitialDelayMs,
        Long marketPriceMaxStalenessMs,
        boolean marketCandleEnabled,
        Long marketCandleFixedDelayMs,
        Long marketCandleInitialDelayMs,
        boolean marketCandleStartupBackfillEnabled,
        Integer marketCandleGapFillOverlapCount,
        boolean macroEnabled,
        Long macroFixedDelayMs,
        Long macroInitialDelayMs,
        boolean sentimentEnabled,
        Long sentimentFixedDelayMs,
        Long sentimentInitialDelayMs,
        boolean onchainEnabled,
        Long onchainFixedDelayMs,
        Long onchainInitialDelayMs
) {

    public ExternalRawIngestionProperties {
        marketPriceFixedDelayMs = marketPriceFixedDelayMs == null || marketPriceFixedDelayMs <= 0 ? 60000L : marketPriceFixedDelayMs;
        marketPriceInitialDelayMs = marketPriceInitialDelayMs == null || marketPriceInitialDelayMs < 0 ? 0L : marketPriceInitialDelayMs;
        marketPriceMaxStalenessMs = marketPriceMaxStalenessMs == null || marketPriceMaxStalenessMs <= 0 ? 900000L : marketPriceMaxStalenessMs;
        marketCandleFixedDelayMs = marketCandleFixedDelayMs == null || marketCandleFixedDelayMs <= 0 ? 300000L : marketCandleFixedDelayMs;
        marketCandleInitialDelayMs = marketCandleInitialDelayMs == null || marketCandleInitialDelayMs < 0 ? 0L : marketCandleInitialDelayMs;
        marketCandleGapFillOverlapCount = marketCandleGapFillOverlapCount == null || marketCandleGapFillOverlapCount <= 0
                ? 3
                : marketCandleGapFillOverlapCount;
        macroFixedDelayMs = macroFixedDelayMs == null || macroFixedDelayMs <= 0 ? 3600000L : macroFixedDelayMs;
        macroInitialDelayMs = macroInitialDelayMs == null || macroInitialDelayMs < 0 ? 0L : macroInitialDelayMs;
        sentimentFixedDelayMs = sentimentFixedDelayMs == null || sentimentFixedDelayMs <= 0 ? 21600000L : sentimentFixedDelayMs;
        sentimentInitialDelayMs = sentimentInitialDelayMs == null || sentimentInitialDelayMs < 0 ? 0L : sentimentInitialDelayMs;
        onchainFixedDelayMs = onchainFixedDelayMs == null || onchainFixedDelayMs <= 0 ? 43200000L : onchainFixedDelayMs;
        onchainInitialDelayMs = onchainInitialDelayMs == null || onchainInitialDelayMs < 0 ? 0L : onchainInitialDelayMs;
    }
}

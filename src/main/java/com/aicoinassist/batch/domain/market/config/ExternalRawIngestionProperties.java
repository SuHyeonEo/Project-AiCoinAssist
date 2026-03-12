package com.aicoinassist.batch.domain.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler.external-raw-ingestion")
public record ExternalRawIngestionProperties(
        boolean enabled,
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
        macroFixedDelayMs = macroFixedDelayMs == null || macroFixedDelayMs <= 0 ? 3600000L : macroFixedDelayMs;
        macroInitialDelayMs = macroInitialDelayMs == null || macroInitialDelayMs < 0 ? 0L : macroInitialDelayMs;
        sentimentFixedDelayMs = sentimentFixedDelayMs == null || sentimentFixedDelayMs <= 0 ? 21600000L : sentimentFixedDelayMs;
        sentimentInitialDelayMs = sentimentInitialDelayMs == null || sentimentInitialDelayMs < 0 ? 0L : sentimentInitialDelayMs;
        onchainFixedDelayMs = onchainFixedDelayMs == null || onchainFixedDelayMs <= 0 ? 43200000L : onchainFixedDelayMs;
        onchainInitialDelayMs = onchainInitialDelayMs == null || onchainInitialDelayMs < 0 ? 0L : onchainInitialDelayMs;
    }
}

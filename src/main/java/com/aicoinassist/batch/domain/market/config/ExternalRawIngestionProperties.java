package com.aicoinassist.batch.domain.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler.external-raw-ingestion")
public record ExternalRawIngestionProperties(
        boolean enabled,
        boolean macroEnabled,
        Long macroFixedDelayMs,
        boolean sentimentEnabled,
        Long sentimentFixedDelayMs,
        boolean onchainEnabled,
        Long onchainFixedDelayMs
) {

    public ExternalRawIngestionProperties {
        macroFixedDelayMs = macroFixedDelayMs == null || macroFixedDelayMs <= 0 ? 3600000L : macroFixedDelayMs;
        sentimentFixedDelayMs = sentimentFixedDelayMs == null || sentimentFixedDelayMs <= 0 ? 3600000L : sentimentFixedDelayMs;
        onchainFixedDelayMs = onchainFixedDelayMs == null || onchainFixedDelayMs <= 0 ? 3600000L : onchainFixedDelayMs;
    }
}

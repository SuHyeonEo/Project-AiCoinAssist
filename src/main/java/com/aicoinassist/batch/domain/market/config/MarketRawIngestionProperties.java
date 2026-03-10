package com.aicoinassist.batch.domain.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler.market-raw-ingestion")
public record MarketRawIngestionProperties(
        boolean enabled,
        Long priceFixedDelayMs,
        Long candleFixedDelayMs,
        Integer candleLimit
) {

    public MarketRawIngestionProperties {
        priceFixedDelayMs = priceFixedDelayMs == null || priceFixedDelayMs <= 0 ? 60000L : priceFixedDelayMs;
        candleFixedDelayMs = candleFixedDelayMs == null || candleFixedDelayMs <= 0 ? 600000L : candleFixedDelayMs;
        candleLimit = candleLimit == null || candleLimit <= 0 ? 3 : candleLimit;
    }
}

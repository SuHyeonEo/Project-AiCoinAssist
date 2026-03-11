package com.aicoinassist.batch.domain.market.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.scheduler.market-raw-ingestion")
public record MarketRawIngestionProperties(
        boolean enabled,
        Long priceFixedDelayMs,
        Long candleFixedDelayMs,
        Integer candleLimit,
        Integer fourHourCandleLimit,
        Integer oneDayCandleLimit
) {

    public MarketRawIngestionProperties {
        priceFixedDelayMs = priceFixedDelayMs == null || priceFixedDelayMs <= 0 ? 60000L : priceFixedDelayMs;
        candleFixedDelayMs = candleFixedDelayMs == null || candleFixedDelayMs <= 0 ? 600000L : candleFixedDelayMs;
        candleLimit = candleLimit == null || candleLimit <= 0 ? 168 : candleLimit;
        fourHourCandleLimit = fourHourCandleLimit == null || fourHourCandleLimit <= 0 ? 180 : fourHourCandleLimit;
        oneDayCandleLimit = oneDayCandleLimit == null || oneDayCandleLimit <= 0 ? 364 : oneDayCandleLimit;
    }
}

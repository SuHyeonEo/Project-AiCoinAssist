package com.aicoinassist.batch.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.coinmetrics")
public record CoinMetricsProperties(
        String baseUrl
) {

    public CoinMetricsProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://community-api.coinmetrics.io/v4"
                : baseUrl;
    }
}

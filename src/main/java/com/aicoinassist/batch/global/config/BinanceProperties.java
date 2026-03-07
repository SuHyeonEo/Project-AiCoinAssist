package com.aicoinassist.batch.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.binance")
public record BinanceProperties(
        String baseUrl
) {
}
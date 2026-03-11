package com.aicoinassist.batch.domain.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.reference-news.manual-generate")
public record ReferenceNewsManualGenerateProperties(
        boolean enabled,
        boolean shutdownAfterRun
) {
}

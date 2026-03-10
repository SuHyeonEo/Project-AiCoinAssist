package com.aicoinassist.batch.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.gdelt")
public record GdeltProperties(
        String baseUrl,
        Integer maxRecords,
        String timeSpan
) {

    public GdeltProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.gdeltproject.org/api/v2/doc/doc"
                : baseUrl;
        maxRecords = maxRecords == null || maxRecords <= 0
                ? 10
                : maxRecords;
        timeSpan = timeSpan == null || timeSpan.isBlank()
                ? "3days"
                : timeSpan;
    }
}

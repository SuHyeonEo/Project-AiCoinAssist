package com.aicoinassist.batch.global.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "external.openai")
public record OpenAiProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        String organization,
        String project,
        Integer connectTimeoutMillis,
        Integer readTimeoutMillis
) {

    public OpenAiProperties {
        apiKey = stripSurroundingQuotes(apiKey);
        organization = stripSurroundingQuotes(organization);
        project = stripSurroundingQuotes(project);
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.openai.com"
                : baseUrl;
        model = model == null || model.isBlank()
                ? "gpt-5.4"
                : model;
        connectTimeoutMillis = connectTimeoutMillis == null || connectTimeoutMillis < 1000
                ? 5000
                : connectTimeoutMillis;
        readTimeoutMillis = readTimeoutMillis == null || readTimeoutMillis < 1000
                ? 120000
                : readTimeoutMillis;
    }

    @AssertTrue(message = "apiKey must be provided when OpenAI narrative gateway is enabled.")
    public boolean hasApiKeyWhenEnabled() {
        return !enabled || (apiKey != null && !apiKey.isBlank());
    }

    private static String stripSurroundingQuotes(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return trimmed.substring(1, trimmed.length() - 1).strip();
            }
        }
        return trimmed;
    }
}

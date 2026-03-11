package com.aicoinassist.batch.domain.news.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.reference-news")
public record ReferenceNewsProperties(
        boolean enabled,
        @NotBlank String provider,
        @NotBlank String scope,
        @NotBlank String promptTemplateVersion,
        @NotBlank String inputSchemaVersion,
        @NotBlank String outputSchemaVersion,
        int maxTransportAttempts
) {

    public ReferenceNewsProperties {
        provider = provider == null || provider.isBlank() ? "openai" : provider;
        scope = scope == null || scope.isBlank() ? "GLOBAL_CRYPTO" : scope;
        promptTemplateVersion = promptTemplateVersion == null || promptTemplateVersion.isBlank()
                ? "reference-news-prompt-v1"
                : promptTemplateVersion;
        inputSchemaVersion = inputSchemaVersion == null || inputSchemaVersion.isBlank()
                ? "reference-news-input-v1"
                : inputSchemaVersion;
        outputSchemaVersion = outputSchemaVersion == null || outputSchemaVersion.isBlank()
                ? "reference-news-output-v1"
                : outputSchemaVersion;
        maxTransportAttempts = maxTransportAttempts <= 0 ? 1 : maxTransportAttempts;
    }
}

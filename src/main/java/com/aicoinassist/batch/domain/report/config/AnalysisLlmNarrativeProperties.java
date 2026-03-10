package com.aicoinassist.batch.domain.report.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.llm-narrative")
public record AnalysisLlmNarrativeProperties(
        @NotBlank String provider,
        @NotBlank String promptTemplateVersion,
        @NotBlank String inputSchemaVersion,
        @NotBlank String outputSchemaVersion
) {

    public AnalysisLlmNarrativeProperties {
        provider = provider == null || provider.isBlank() ? "openai" : provider;
        promptTemplateVersion = promptTemplateVersion == null || promptTemplateVersion.isBlank()
                ? "llm-prompt-v1"
                : promptTemplateVersion;
        inputSchemaVersion = inputSchemaVersion == null || inputSchemaVersion.isBlank()
                ? "llm-input-v1"
                : inputSchemaVersion;
        outputSchemaVersion = outputSchemaVersion == null || outputSchemaVersion.isBlank()
                ? "llm-output-v1"
                : outputSchemaVersion;
    }
}

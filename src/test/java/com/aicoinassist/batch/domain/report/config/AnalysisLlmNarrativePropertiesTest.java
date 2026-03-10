package com.aicoinassist.batch.domain.report.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmNarrativePropertiesTest {

    @Test
    void appliesDefaultsForBlankValues() {
        AnalysisLlmNarrativeProperties properties = new AnalysisLlmNarrativeProperties(
                false,
                " ",
                "",
                null,
                " "
        );

        assertThat(properties.enabled()).isFalse();
        assertThat(properties.provider()).isEqualTo("openai");
        assertThat(properties.promptTemplateVersion()).isEqualTo("llm-prompt-v1");
        assertThat(properties.inputSchemaVersion()).isEqualTo("llm-input-v1");
        assertThat(properties.outputSchemaVersion()).isEqualTo("llm-output-v1");
    }
}

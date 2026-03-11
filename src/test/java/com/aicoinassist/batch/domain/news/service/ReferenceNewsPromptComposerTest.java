package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsPromptComposition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceNewsPromptComposerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void composeBuildsFiveItemContract() {
        ReferenceNewsPromptComposer composer = new ReferenceNewsPromptComposer(
                new ReferenceNewsProperties(
                        false,
                        "openai",
                        "GLOBAL_CRYPTO",
                        "reference-news-prompt-v1",
                        "reference-news-input-v1",
                        "reference-news-output-v1",
                        1
                ),
                objectMapper
        );

        ReferenceNewsPromptComposition composition = composer.compose(LocalDate.parse("2026-03-12"));

        assertThat(composition.inputPayloadJson()).contains("\"scope\":\"GLOBAL_CRYPTO\"");
        assertThat(composition.inputPayloadJson()).contains("\"snapshot_date\":\"2026-03-12\"");
        assertThat(composition.outputSchemaJson()).contains("\"minItems\": 5");
        assertThat(composition.outputSchemaJson()).contains("\"selection_reason\"");
        assertThat(composition.outputLengthPolicyJson()).contains("\"maxItems\":5");
    }
}

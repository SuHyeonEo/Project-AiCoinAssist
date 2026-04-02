package com.aicoinassist.batch.global.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void allowsDisabledGatewayWithoutApiKey() {
        OpenAiProperties properties = new OpenAiProperties(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<OpenAiProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
        assertThat(properties.baseUrl()).isEqualTo("https://api.openai.com");
        assertThat(properties.model()).isEqualTo("gpt-5.4");
        assertThat(properties.readTimeoutMillis()).isEqualTo(120000);
    }

    @Test
    void rejectsEnabledGatewayWithoutApiKey() {
        OpenAiProperties properties = new OpenAiProperties(
                true,
                null,
                " ",
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<OpenAiProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("apiKey must be provided");
    }

    @Test
    void stripsSurroundingQuotesFromApiKeyAndProjectMetadata() {
        OpenAiProperties properties = new OpenAiProperties(
                true,
                null,
                "\"sk-test-key\"",
                null,
                "\"org_test\"",
                "\"proj_test\"",
                null,
                null
        );

        Set<ConstraintViolation<OpenAiProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
        assertThat(properties.apiKey()).isEqualTo("sk-test-key");
        assertThat(properties.organization()).isEqualTo("org_test");
        assertThat(properties.project()).isEqualTo("proj_test");
    }
}

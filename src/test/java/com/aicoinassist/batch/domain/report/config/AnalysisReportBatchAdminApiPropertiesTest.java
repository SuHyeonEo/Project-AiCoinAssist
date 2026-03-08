package com.aicoinassist.batch.domain.report.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportBatchAdminApiPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void allowsDisabledProtectionWithoutToken() {
        AnalysisReportBatchAdminApiProperties properties = new AnalysisReportBatchAdminApiProperties(
                false,
                null,
                null
        );

        Set<ConstraintViolation<AnalysisReportBatchAdminApiProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsEnabledProtectionWithoutToken() {
        AnalysisReportBatchAdminApiProperties properties = new AnalysisReportBatchAdminApiProperties(
                true,
                " ",
                "X-Admin-Token"
        );

        Set<ConstraintViolation<AnalysisReportBatchAdminApiProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("token must be provided");
    }
}

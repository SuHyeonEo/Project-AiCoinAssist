package com.aicoinassist.batch.domain.report.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportBatchManualRerunPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void allowsDisabledManualRerunWithoutSourceRunId() {
        AnalysisReportBatchManualRerunProperties properties = new AnalysisReportBatchManualRerunProperties(
                false,
                null,
                true
        );

        Set<ConstraintViolation<AnalysisReportBatchManualRerunProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsEnabledManualRerunWithoutSourceRunId() {
        AnalysisReportBatchManualRerunProperties properties = new AnalysisReportBatchManualRerunProperties(
                true,
                " ",
                true
        );

        Set<ConstraintViolation<AnalysisReportBatchManualRerunProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("sourceRunId must be provided");
    }
}

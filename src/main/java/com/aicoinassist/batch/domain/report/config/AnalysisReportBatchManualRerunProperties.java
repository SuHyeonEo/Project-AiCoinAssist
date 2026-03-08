package com.aicoinassist.batch.domain.report.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.analysis-report.manual-rerun")
public record AnalysisReportBatchManualRerunProperties(
        boolean enabled,
        String sourceRunId,
        boolean shutdownAfterRun
) {

    @AssertTrue(message = "sourceRunId must be provided when manual rerun is enabled.")
    public boolean hasSourceRunIdWhenEnabled() {
        return !enabled || (sourceRunId != null && !sourceRunId.isBlank());
    }
}

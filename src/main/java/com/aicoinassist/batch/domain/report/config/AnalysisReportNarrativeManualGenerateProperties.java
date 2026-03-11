package com.aicoinassist.batch.domain.report.config;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.llm-narrative.manual-generate")
public record AnalysisReportNarrativeManualGenerateProperties(
        boolean enabled,
        String symbol,
        AnalysisReportType reportType,
        boolean shutdownAfterRun
) {

    @AssertTrue(message = "symbol and reportType must be provided when manual latest narrative generation is enabled.")
    public boolean hasRequiredFieldsWhenEnabled() {
        return !enabled || (symbol != null && !symbol.isBlank() && reportType != null);
    }
}

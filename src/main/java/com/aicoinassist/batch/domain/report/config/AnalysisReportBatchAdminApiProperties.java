package com.aicoinassist.batch.domain.report.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.admin-api")
public record AnalysisReportBatchAdminApiProperties(
        boolean enabled,
        String token,
        String headerName
) {

    @AssertTrue(message = "token must be provided when admin API protection is enabled.")
    public boolean hasTokenWhenEnabled() {
        return !enabled || (token != null && !token.isBlank());
    }

    @AssertTrue(message = "headerName must be provided when admin API protection is enabled.")
    public boolean hasHeaderNameWhenEnabled() {
        return !enabled || (headerName != null && !headerName.isBlank());
    }

    @AssertTrue(message = "token must not use an insecure placeholder value when admin API protection is enabled.")
    public boolean hasSecureTokenWhenEnabled() {
        return !enabled || !"change-me".equals(token);
    }
}

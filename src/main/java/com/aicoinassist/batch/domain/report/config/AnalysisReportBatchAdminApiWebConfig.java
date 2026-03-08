package com.aicoinassist.batch.domain.report.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AnalysisReportBatchAdminApiWebConfig implements WebMvcConfigurer {

    private final AnalysisReportBatchAdminApiAuthInterceptor authInterceptor;
    private final AnalysisReportBatchAdminApiAuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/internal/admin/**");
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/internal/admin/**");
    }
}

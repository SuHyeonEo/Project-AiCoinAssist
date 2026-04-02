package com.aicoinassist.batch.domain.report.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AnalysisReportBatchAdminApiAuthInterceptor implements HandlerInterceptor {

    private final AnalysisReportBatchAdminApiProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!properties.enabled()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                    {"type":"about:blank","title":"Not Found","status":404,"detail":"Admin API is disabled."}
                    """.trim());
            return false;
        }

        String providedToken = request.getHeader(properties.headerName());
        if (properties.token().equals(providedToken)) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.getWriter().write("""
                {"type":"about:blank","title":"Unauthorized admin API access","status":401,"detail":"Valid admin API token is required."}
                """.trim());
        return false;
    }
}

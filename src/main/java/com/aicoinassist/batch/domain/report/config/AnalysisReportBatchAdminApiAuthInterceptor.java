package com.aicoinassist.batch.domain.report.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
            return true;
        }

        String providedToken = request.getHeader(properties.headerName());
        if (properties.token().equals(providedToken)) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Unauthorized admin API access");
        problemDetail.setDetail("Valid admin API token is required.");
        response.getWriter().write("""
                {"type":"about:blank","title":"%s","status":401,"detail":"%s"}
                """.formatted(problemDetail.getTitle(), problemDetail.getDetail()).trim());
        return false;
    }
}

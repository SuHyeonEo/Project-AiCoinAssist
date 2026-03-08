package com.aicoinassist.batch.domain.report.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AnalysisReportBatchAdminApiAuditInterceptor implements HandlerInterceptor {

    private static final String REQUEST_START_TIME_ATTR =
            AnalysisReportBatchAdminApiAuditInterceptor.class.getName() + ".requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(REQUEST_START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        long durationMillis = durationMillis(request);
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();
        String runId = extractRunId(requestUri);
        boolean tokenPresent = request.getHeader("X-Admin-Token") != null;

        if (isRerunRequest(method, requestUri)) {
            log.warn(
                    "admin api rerun request - method: {}, path: {}, runId: {}, status: {}, durationMs: {}, remoteAddr: {}, tokenPresent: {}",
                    method,
                    requestUri,
                    runId,
                    response.getStatus(),
                    durationMillis,
                    remoteAddr,
                    tokenPresent
            );
            return;
        }

        log.info(
                "admin api request - method: {}, path: {}, runId: {}, status: {}, durationMs: {}, remoteAddr: {}, tokenPresent: {}",
                method,
                requestUri,
                runId,
                response.getStatus(),
                durationMillis,
                remoteAddr,
                tokenPresent
        );
    }

    private static boolean isRerunRequest(String method, String requestUri) {
        return "POST".equalsIgnoreCase(method) && requestUri.endsWith("/rerun-failed");
    }

    private static long durationMillis(HttpServletRequest request) {
        Object startTimeAttr = request.getAttribute(REQUEST_START_TIME_ATTR);
        if (startTimeAttr instanceof Long startTime) {
            return Math.max(0L, System.currentTimeMillis() - startTime);
        }
        return 0L;
    }

    private static String extractRunId(String requestUri) {
        String prefix = "/internal/admin/report-batch-runs/";
        if (!requestUri.startsWith(prefix)) {
            return null;
        }
        String remaining = requestUri.substring(prefix.length());
        if (remaining.isBlank()) {
            return null;
        }
        int slashIndex = remaining.indexOf('/');
        if (slashIndex < 0) {
            return remaining;
        }
        return remaining.substring(0, slashIndex);
    }
}

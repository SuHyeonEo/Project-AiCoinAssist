package com.aicoinassist.batch.domain.report.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class AnalysisReportBatchAdminApiAuditInterceptorTest {

    private final AnalysisReportBatchAdminApiAuditInterceptor interceptor =
            new AnalysisReportBatchAdminApiAuditInterceptor();
    private final Logger logger = (Logger) LoggerFactory.getLogger(AnalysisReportBatchAdminApiAuditInterceptor.class);

    @Test
    void afterCompletionLogsGenericAdminRequest(CapturedOutput output) throws Exception {
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.INFO);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/admin/report-batch-runs/run-001");
        request.addHeader("X-Admin-Token", "token-present");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        try {
            interceptor.preHandle(request, response, new Object());
            interceptor.afterCompletion(request, response, new Object(), null);
        } finally {
            logger.setLevel(previousLevel);
        }

        assertThat(output.getOut()).contains("admin api request");
        assertThat(output.getOut()).contains("runId: run-001");
        assertThat(output.getOut()).contains("tokenPresent: true");
    }

    @Test
    void afterCompletionLogsRerunRequestAsWarn(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/admin/report-batch-runs/run-002/rerun-failed");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(202);

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(output.getOut()).contains("admin api rerun request");
        assertThat(output.getOut()).contains("runId: run-002");
        assertThat(output.getOut()).contains("tokenPresent: false");
    }
}

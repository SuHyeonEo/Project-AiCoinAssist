package com.aicoinassist.batch.domain.report.controller;

import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiAuditInterceptor;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiAuthInterceptor;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiProperties;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchAdminApiWebConfig;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeView;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportNarrativeReadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalysisReportNarrativeAdminController.class)
@EnableConfigurationProperties(AnalysisReportBatchAdminApiProperties.class)
@Import({
        AnalysisReportBatchAdminExceptionHandler.class,
        AnalysisReportBatchAdminApiWebConfig.class,
        AnalysisReportBatchAdminApiAuthInterceptor.class,
        AnalysisReportBatchAdminApiAuditInterceptor.class
})
@TestPropertySource(properties = {
        "batch.admin-api.enabled=true",
        "batch.admin-api.token=test-admin-token",
        "batch.admin-api.header-name=X-Admin-Token"
})
class AnalysisReportNarrativeAdminControllerTest {

    private static final String ADMIN_HEADER = "X-Admin-Token";
    private static final String ADMIN_TOKEN = "test-admin-token";
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisReportNarrativeReadService analysisReportNarrativeReadService;

    @Test
    void getLatestNarrativeReturnsNarrativeView() throws Exception {
        when(analysisReportNarrativeReadService.getLatest("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(view(1L));

        mockMvc.perform(get("/internal/admin/report-narratives/latest")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .param("symbol", "BTCUSDT")
                        .param("reportType", "SHORT_TERM")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reportType").value("SHORT_TERM"))
                .andExpect(jsonPath("$.generationStatus").value("SUCCESS"));
    }

    @Test
    void getNarrativeDetailReturnsNotFoundWhenMissing() throws Exception {
        when(analysisReportNarrativeReadService.getById(99L))
                .thenThrow(new AnalysisReportNarrativeNotFoundException("Report narrative not found: 99"));

        mockMvc.perform(get("/internal/admin/report-narratives/99")
                        .header(ADMIN_HEADER, ADMIN_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Report narrative not found"));
    }

    private AnalysisReportNarrativeView view(Long id) throws Exception {
        return new AnalysisReportNarrativeView(
                id,
                10L,
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                "basis-key",
                "gpt-5.4",
                "openai",
                "gpt-5.4",
                "llm-prompt-v1",
                "llm-input-v1",
                "llm-output-v1",
                false,
                AnalysisLlmNarrativeGenerationStatus.SUCCESS,
                AnalysisLlmNarrativeFailureType.NONE,
                "req-1",
                1000,
                500,
                1500,
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:03Z"),
                Instant.parse("2026-03-09T01:00:05Z"),
                objectMapper.readTree("{\"metadata\":{\"symbol\":\"BTCUSDT\"}}"),
                "system",
                "user",
                objectMapper.readTree("{\"executive_conclusion\":{\"max_sentences\":3}}"),
                objectMapper.readTree("[]"),
                "{\"raw\":true}",
                objectMapper.readTree("{\"executive_conclusion\":{\"overall_tone\":\"mixed\"}}"),
                objectMapper.readTree("[]")
        );
    }
}

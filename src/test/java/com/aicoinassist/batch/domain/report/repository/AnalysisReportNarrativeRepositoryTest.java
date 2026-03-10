package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AnalysisReportNarrativeRepositoryTest {

    @Autowired
    private AnalysisReportRepository analysisReportRepository;

    @Autowired
    private AnalysisReportNarrativeRepository analysisReportNarrativeRepository;

    @Test
    void rejectsDuplicateNarrativeIdentity() {
        AnalysisReportEntity reportEntity = analysisReportRepository.saveAndFlush(report());

        analysisReportNarrativeRepository.saveAndFlush(narrative(reportEntity, "{\"summary\":\"first\"}"));

        assertThatThrownBy(() -> analysisReportNarrativeRepository.saveAndFlush(
                narrative(reportEntity, "{\"summary\":\"second\"}")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private AnalysisReportEntity report() {
        return AnalysisReportEntity.builder()
                .symbol("BTCUSDT")
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .rawReferenceTime(Instant.parse("2026-03-09T00:59:30Z"))
                .sourceDataVersion("basis-key")
                .analysisEngineVersion("gpt-5.4")
                .reportPayload("{\"summary\":\"report\"}")
                .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                .build();
    }

    private AnalysisReportNarrativeEntity narrative(AnalysisReportEntity reportEntity, String outputJson) {
        return AnalysisReportNarrativeEntity.builder()
                .analysisReport(reportEntity)
                .symbol("BTCUSDT")
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .sourceDataVersion("basis-key")
                .analysisEngineVersion("gpt-5.4")
                .llmProvider("OPENAI")
                .llmModel("gpt-5.4")
                .promptTemplateVersion("prompt-v1")
                .inputSchemaVersion("llm-input-v1")
                .outputSchemaVersion("llm-output-v1")
                .inputPayloadHash("fixed-hash")
                .inputPayloadJson("{\"input\":true}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{\"policy\":true}")
                .referenceNewsJson("[]")
                .rawOutputText("{\"raw\":true}")
                .outputJson(outputJson)
                .fallbackUsed(false)
                .generationStatus(AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                .failureType(AnalysisLlmNarrativeFailureType.NONE)
                .validationIssuesJson("[]")
                .providerRequestId("req-1")
                .inputTokens(1200)
                .outputTokens(700)
                .totalTokens(1900)
                .requestedAt(Instant.parse("2026-03-09T01:00:00Z"))
                .completedAt(Instant.parse("2026-03-09T01:00:05Z"))
                .storedAt(Instant.parse("2026-03-09T01:00:10Z"))
                .build();
    }
}

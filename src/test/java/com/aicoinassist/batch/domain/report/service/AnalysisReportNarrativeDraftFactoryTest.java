package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportNarrativeDraftFactoryTest extends AnalysisReportPayloadTestFixtures {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisReportNarrativeDraftFactory factory = new AnalysisReportNarrativeDraftFactory(objectMapper);

    @Test
    void createBuildsNarrativeDraftFromGenerationResult() throws Exception {
        AnalysisReportEntity reportEntity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "basis-key",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        setId(reportEntity, 1L);

        AnalysisLlmNarrativeInputPayload input = new AnalysisLlmNarrativeInputAssembler().assemble(
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                        .assemble(reportEntity, shortTermPayload("Narrative summary")),
                new com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextReference(
                        "shared-v1",
                        "공통 시장 해설",
                        new com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference(
                                "MIXED",
                                "거시 혼조",
                                "달러 확인"
                        ),
                        new com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference(
                                "MIXED",
                                "심리 혼조",
                                "심리 확인"
                        )
                )
        );
        AnalysisLlmNarrativeGenerationResult generationResult = new AnalysisLlmNarrativeGenerationServiceTestSupport(objectMapper)
                .successfulGenerationResult(input);

        AnalysisReportNarrativeDraft draft = factory.create(
                reportEntity,
                generationResult,
                "OPENAI",
                "gpt-5.4",
                "prompt-v1",
                "llm-input-v1",
                "llm-output-v1",
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:05Z"),
                Instant.parse("2026-03-09T01:00:10Z")
        );

        assertThat(draft.analysisReport()).isSameAs(reportEntity);
        assertThat(draft.sharedContextId()).isEqualTo(1L);
        assertThat(draft.sharedContextVersion()).isEqualTo("shared-v1");
        assertThat(draft.llmProvider()).isEqualTo("OPENAI");
        assertThat(draft.llmModel()).isEqualTo("gpt-5.4");
        assertThat(draft.promptTemplateVersion()).isEqualTo("prompt-v1");
        assertThat(draft.referenceNewsJson()).isEqualTo("[]");
        assertThat(draft.outputPayload()).isNotNull();
        assertThat(draft.generationStatus().name()).isEqualTo("SUCCESS");
        assertThat(draft.failureType()).isEqualTo(AnalysisLlmNarrativeFailureType.NONE);
        assertThat(draft.totalTokens()).isEqualTo(1900);
    }

    private void setId(AnalysisReportEntity entity, Long id) throws Exception {
        Field field = AnalysisReportEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}

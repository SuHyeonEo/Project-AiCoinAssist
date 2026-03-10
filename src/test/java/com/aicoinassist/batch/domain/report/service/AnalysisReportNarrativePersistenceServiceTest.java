package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportNarrativePersistenceServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisReportNarrativeRepository analysisReportNarrativeRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void saveRefreshesExistingNarrativeWhenIdentityMatches() throws Exception {
        AnalysisReportNarrativePersistenceService service =
                new AnalysisReportNarrativePersistenceService(analysisReportNarrativeRepository, objectMapper);

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

        AnalysisReportNarrativeDraft draft = draft(reportEntity, "Updated summary");
        AnalysisReportNarrativeEntity existingEntity = AnalysisReportNarrativeEntity.builder()
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
                .inputPayloadHash("hash")
                .inputPayloadJson("{\"old\":true}")
                .promptSystemText("old")
                .promptUserText("old")
                .outputLengthPolicyJson("{\"old\":true}")
                .referenceNewsJson("[]")
                .rawOutputText("{\"old\":true}")
                .outputJson("{\"old\":true}")
                .fallbackUsed(false)
                .generationStatus(AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                .failureType(AnalysisLlmNarrativeFailureType.NONE)
                .validationIssuesJson("[]")
                .providerRequestId("req-old")
                .inputTokens(1)
                .outputTokens(1)
                .totalTokens(2)
                .requestedAt(Instant.parse("2026-03-09T01:00:00Z"))
                .completedAt(Instant.parse("2026-03-09T01:00:01Z"))
                .storedAt(Instant.parse("2026-03-09T01:00:02Z"))
                .build();

        when(analysisReportNarrativeRepository
                .findTopByAnalysisReportIdAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        any(), any(), any(), any(), any(), any(), any()
                )).thenReturn(Optional.of(existingEntity));

        AnalysisReportNarrativeEntity result = service.save(draft);

        verify(analysisReportNarrativeRepository, never()).save(any(AnalysisReportNarrativeEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getPromptUserText()).contains("Input JSON:");
        assertThat(existingEntity.getOutputJson()).contains("Short summary");
        assertThat(existingEntity.getTotalTokens()).isEqualTo(1900);
    }

    @Test
    void savePersistsNewNarrativeWhenIdentityDoesNotExist() throws Exception {
        AnalysisReportNarrativePersistenceService service =
                new AnalysisReportNarrativePersistenceService(analysisReportNarrativeRepository, objectMapper);

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

        AnalysisReportNarrativeDraft draft = draft(reportEntity, "New summary");

        when(analysisReportNarrativeRepository
                .findTopByAnalysisReportIdAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        any(), any(), any(), any(), any(), any(), any()
                )).thenReturn(Optional.empty());
        when(analysisReportNarrativeRepository.save(any(AnalysisReportNarrativeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisReportNarrativeEntity result = service.save(draft);

        assertThat(result.getAnalysisReport()).isSameAs(reportEntity);
        assertThat(result.getLlmProvider()).isEqualTo("OPENAI");
        assertThat(result.getLlmModel()).isEqualTo("gpt-5.4");
        assertThat(result.getPromptTemplateVersion()).isEqualTo("prompt-v1");
        assertThat(result.getGenerationStatus()).isEqualTo(AnalysisLlmNarrativeGenerationStatus.SUCCESS);
        assertThat(result.getOutputJson()).contains("Short summary");
        assertThat(result.getReferenceNewsJson()).contains("ETF delay headline");
    }

    private AnalysisReportNarrativeDraft draft(AnalysisReportEntity reportEntity, String summary) throws Exception {
        AnalysisLlmNarrativeGenerationServiceTestSupport support = new AnalysisLlmNarrativeGenerationServiceTestSupport(objectMapper);
        var input = new AnalysisLlmNarrativeInputAssembler().assemble(
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                        .assemble(reportEntity, shortTermPayload(summary))
        );
        return new AnalysisReportNarrativeDraftFactory(objectMapper).create(
                reportEntity,
                support.successfulGenerationResult(input),
                "OPENAI",
                "gpt-5.4",
                "prompt-v1",
                "llm-input-v1",
                "llm-output-v1",
                java.util.List.of(new com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem(
                        "ETF delay headline",
                        "ExampleSource",
                        Instant.parse("2026-03-09T00:30:00Z"),
                        "https://example.com/news/1",
                        "Why it matters",
                        "MACRO"
                )),
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:05Z"),
                Instant.parse("2026-03-09T01:00:10Z")
        );
    }

    private void setId(AnalysisReportEntity entity, Long id) throws Exception {
        Field field = AnalysisReportEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}

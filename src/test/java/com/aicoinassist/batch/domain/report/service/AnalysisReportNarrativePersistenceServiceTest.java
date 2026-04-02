package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportSharedContextEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportSharedContextRepository;
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

    @Mock
    private AnalysisReportSharedContextRepository analysisReportSharedContextRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void saveRefreshesExistingNarrativeWhenIdentityMatches() throws Exception {
        AnalysisReportNarrativePersistenceService service =
                new AnalysisReportNarrativePersistenceService(
                        analysisReportNarrativeRepository,
                        analysisReportSharedContextRepository,
                        objectMapper
                );

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
        when(analysisReportSharedContextRepository.findById(1L)).thenReturn(Optional.of(sharedContextEntity()));
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
                new AnalysisReportNarrativePersistenceService(
                        analysisReportNarrativeRepository,
                        analysisReportSharedContextRepository,
                        objectMapper
                );

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
        when(analysisReportSharedContextRepository.findById(1L)).thenReturn(Optional.of(sharedContextEntity()));

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
        assertThat(result.getSharedContext()).isNotNull();
        assertThat(result.getSharedContext().getId()).isEqualTo(1L);
        assertThat(result.getGenerationStatus()).isEqualTo(AnalysisLlmNarrativeGenerationStatus.SUCCESS);
        assertThat(result.getOutputJson()).contains("Short summary");
        assertThat(result.getReferenceNewsJson()).isEqualTo("[]");
    }

    private AnalysisReportNarrativeDraft draft(AnalysisReportEntity reportEntity, String summary) throws Exception {
        AnalysisLlmNarrativeGenerationServiceTestSupport support = new AnalysisLlmNarrativeGenerationServiceTestSupport(objectMapper);
        var input = new AnalysisLlmNarrativeInputAssembler().assemble(
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                        .assemble(reportEntity, shortTermPayload(summary)),
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
        return new AnalysisReportNarrativeDraftFactory(objectMapper).create(
                reportEntity,
                support.successfulGenerationResult(input),
                "OPENAI",
                "gpt-5.4",
                "prompt-v1",
                "llm-input-v1",
                "llm-output-v1",
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:05Z"),
                Instant.parse("2026-03-09T01:00:10Z")
        );
    }

    private AnalysisReportSharedContextEntity sharedContextEntity() {
        AnalysisReportSharedContextEntity entity = AnalysisReportSharedContextEntity.builder()
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .rawReferenceTime(Instant.parse("2026-03-09T00:59:30Z"))
                .contextVersion("shared-v1")
                .analysisEngineVersion("gpt-5.4")
                .llmProvider("OPENAI")
                .llmModel("gpt-5.4")
                .promptTemplateVersion("prompt-v1-shared-context")
                .inputSchemaVersion("llm-input-v1-shared-context")
                .outputSchemaVersion("llm-output-v1-shared-context")
                .inputPayloadHash("hash")
                .inputPayloadJson("{}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{}")
                .rawOutputText(null)
                .outputJson("{}")
                .fallbackUsed(false)
                .generationStatus(AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                .failureType(AnalysisLlmNarrativeFailureType.NONE)
                .validationIssuesJson("[]")
                .providerRequestId("req-shared")
                .inputTokens(10)
                .outputTokens(10)
                .totalTokens(20)
                .requestedAt(Instant.parse("2026-03-09T01:00:00Z"))
                .completedAt(Instant.parse("2026-03-09T01:00:01Z"))
                .storedAt(Instant.parse("2026-03-09T01:00:02Z"))
                .build();
        try {
            Field field = AnalysisReportSharedContextEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, 1L);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        return entity;
    }

    private void setId(AnalysisReportEntity entity, Long id) throws Exception {
        Field field = AnalysisReportEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextResolution;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportSharedContextEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportSharedContextRepository;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisLlmSharedContextGenerationServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisLlmNarrativeGateway analysisLlmNarrativeGateway;

    @Mock
    private AnalysisReportSharedContextRepository analysisReportSharedContextRepository;

    @Mock
    private AnalysisReportSharedContextPersistenceService analysisReportSharedContextPersistenceService;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void getOrGenerateReusesStoredSharedContextAcrossDifferentAssetReportTimes() throws Exception {
        AnalysisLlmSharedContextGenerationService service = service();
        AnalysisReportSharedContextEntity storedEntity = sharedContextEntity(sharedContextReference());
        when(analysisLlmNarrativeGateway.generate(any()))
                .thenReturn(new AnalysisLlmNarrativeGatewayResponse(
                        """
                        {
                          "shared_summary": "거시와 심리 공통 맥락은 혼조로 정리됩니다.",
                          "macro": {
                            "status": "MIXED",
                            "summary": "거시 조합은 뚜렷한 한 방향보다 혼조에 가깝습니다.",
                            "watch_point": "달러와 금리 흐름을 함께 확인할 필요가 있습니다."
                          },
                          "sentiment": {
                            "status": "MIXED",
                            "summary": "심리 지표는 아직 확신을 주지 못하고 있습니다.",
                            "watch_point": "심리 개선 여부를 추가로 확인할 필요가 있습니다."
                          }
                        }
                        """,
                        "gpt-5-mini",
                        "req-shared-1",
                        100,
                        80
                ));
        when(analysisReportSharedContextRepository
                .findTopByReportTypeAndContextVersionAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty(), Optional.of(storedEntity));
        when(analysisReportSharedContextPersistenceService.save(any()))
                .thenReturn(storedEntity);

        AnalysisLlmSharedContextResolution first = service.getOrGenerate(reportInput(Instant.parse("2026-03-09T01:00:30Z")));
        AnalysisLlmSharedContextResolution second = service.getOrGenerate(reportInput(Instant.parse("2026-03-09T01:02:30Z")));

        assertThat(second).isEqualTo(first);
        assertThat(second.sharedContextId()).isEqualTo(1L);
        assertThat(second.reference().sharedSummary()).contains("거시와 심리 공통 맥락");
        verify(analysisLlmNarrativeGateway, times(1)).generate(any());
        verify(analysisReportSharedContextPersistenceService, times(1)).save(any());
    }

    @Test
    void getOrGenerateUsesStoredSharedContextBeforeCallingGateway() throws Exception {
        AnalysisLlmSharedContextGenerationService service = service();
        AnalysisLlmSharedContextReference storedReference = sharedContextReference();
        when(analysisReportSharedContextRepository
                .findTopByReportTypeAndContextVersionAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
                        any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(sharedContextEntity(storedReference)));

        AnalysisLlmSharedContextResolution result = service.getOrGenerate(reportInput(Instant.parse("2026-03-09T01:00:30Z")));

        assertThat(result.sharedContextId()).isEqualTo(1L);
        assertThat(result.reference()).isEqualTo(storedReference);
        verify(analysisLlmNarrativeGateway, times(0)).generate(any());
        verify(analysisReportSharedContextPersistenceService, times(0)).save(any());
    }

    private AnalysisGptReportInputPayload reportInput(Instant storedTime) {
        return new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                .assemble(
                        reportEntity(
                                AnalysisReportType.SHORT_TERM,
                                Instant.parse("2026-03-09T00:59:59Z"),
                                Instant.parse("2026-03-09T00:59:30Z"),
                                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                                "gpt-5.4",
                                "{\"summary\":\"unused\"}",
                                storedTime
                        ),
                        shortTermPayload("Narrative summary")
                );
    }

    private AnalysisLlmSharedContextGenerationService service() {
        return new AnalysisLlmSharedContextGenerationService(
                new AnalysisLlmNarrativeProperties(
                        true,
                        "openai",
                        "llm-prompt-v1",
                        "llm-input-v1",
                        "llm-output-v1",
                        1
                ),
                new AnalysisLlmSharedContextInputAssembler(),
                new AnalysisLlmSharedContextPromptComposer(objectMapper),
                analysisLlmNarrativeGateway,
                analysisReportSharedContextRepository,
                new AnalysisReportSharedContextDraftFactory(objectMapper),
                analysisReportSharedContextPersistenceService,
                new OpenAiProperties(true, "https://api.openai.com", "test-openai-key", "gpt-5-mini", null, null, 5000, 30000),
                Clock.fixed(Instant.parse("2026-03-11T00:00:00Z"), ZoneOffset.UTC),
                objectMapper
        );
    }

    private AnalysisLlmSharedContextReference sharedContextReference() {
        return new AnalysisLlmSharedContextReference(
                "shared-v1",
                "거시와 심리 공통 맥락은 혼조로 정리됩니다.",
                new com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "거시 조합은 뚜렷한 한 방향보다 혼조에 가깝습니다.",
                        "달러와 금리 흐름을 함께 확인할 필요가 있습니다."
                ),
                new com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "심리 지표는 아직 확신을 주지 못하고 있습니다.",
                        "심리 개선 여부를 추가로 확인할 필요가 있습니다."
                )
        );
    }

    private AnalysisReportSharedContextEntity sharedContextEntity(AnalysisLlmSharedContextReference output) throws Exception {
        AnalysisReportSharedContextEntity entity = AnalysisReportSharedContextEntity.builder()
                .reportType(AnalysisReportType.SHORT_TERM)
                .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                .rawReferenceTime(Instant.parse("2026-03-09T00:59:30Z"))
                .contextVersion(output.contextVersion())
                .analysisEngineVersion("gpt-5.4")
                .llmProvider("openai")
                .llmModel("gpt-5-mini")
                .promptTemplateVersion("llm-prompt-v1-shared-context")
                .inputSchemaVersion("llm-input-v1-shared-context")
                .outputSchemaVersion("llm-output-v1-shared-context")
                .inputPayloadHash("hash")
                .inputPayloadJson("{}")
                .promptSystemText("system")
                .promptUserText("user")
                .outputLengthPolicyJson("{}")
                .rawOutputText(null)
                .outputJson(objectMapper.writeValueAsString(output))
                .fallbackUsed(false)
                .generationStatus(com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                .failureType(null)
                .validationIssuesJson("[]")
                .providerRequestId("req-shared-1")
                .inputTokens(100)
                .outputTokens(80)
                .totalTokens(180)
                .requestedAt(Instant.parse("2026-03-11T00:00:00Z"))
                .completedAt(Instant.parse("2026-03-11T00:00:00Z"))
                .storedAt(Instant.parse("2026-03-11T00:00:00Z"))
                .build();
        Field field = AnalysisReportSharedContextEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, 1L);
        return entity;
    }
}

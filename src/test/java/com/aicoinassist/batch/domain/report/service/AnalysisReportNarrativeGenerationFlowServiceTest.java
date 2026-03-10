package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportNarrativeGenerationFlowServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    @Mock
    private AnalysisLlmNarrativeGenerationService analysisLlmNarrativeGenerationService;

    @Mock
    private AnalysisReportNarrativePersistenceService analysisReportNarrativePersistenceService;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void generateAndStoreLatestUsesNarrativeMetadataAndPersistsDraft() throws Exception {
        AnalysisReportEntity analysisReport = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                objectMapper.writeValueAsString(shortTermPayload("Narrative summary")),
                Instant.parse("2026-03-09T01:00:30Z")
        );
        AnalysisLlmNarrativeGenerationResult generationResult = new AnalysisLlmNarrativeGenerationServiceTestSupport(objectMapper)
                .successfulGenerationResult(
                        new AnalysisLlmNarrativeInputAssembler().assemble(
                                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory()).assemble(
                                        analysisReport,
                                        shortTermPayload("Narrative summary")
                                )
                        )
                );
        when(analysisReportRepository.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(Optional.of(analysisReport));
        when(analysisLlmNarrativeGenerationService.generateLatest("BTCUSDT", AnalysisReportType.SHORT_TERM, List.of()))
                .thenReturn(generationResult);
        when(analysisReportNarrativePersistenceService.save(org.mockito.ArgumentMatchers.any(AnalysisReportNarrativeDraft.class)))
                .thenReturn(
                        AnalysisReportNarrativeEntity.builder()
                                .analysisReport(analysisReport)
                                .symbol("BTCUSDT")
                                .reportType(AnalysisReportType.SHORT_TERM)
                                .analysisBasisTime(analysisReport.getAnalysisBasisTime())
                                .sourceDataVersion(analysisReport.getSourceDataVersion())
                                .analysisEngineVersion(analysisReport.getAnalysisEngineVersion())
                                .llmProvider("openai")
                                .llmModel("gpt-5.4")
                                .promptTemplateVersion("llm-prompt-v1")
                                .inputSchemaVersion("llm-input-v1")
                                .outputSchemaVersion("llm-output-v1")
                                .inputPayloadHash("hash")
                                .inputPayloadJson("{}")
                                .promptSystemText("system")
                                .promptUserText("user")
                                .outputLengthPolicyJson("{}")
                                .referenceNewsJson("[]")
                                .outputJson("{}")
                                .fallbackUsed(false)
                                .generationStatus(com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus.SUCCESS)
                                .validationIssuesJson("[]")
                                .requestedAt(Instant.parse("2026-03-11T00:00:00Z"))
                                .completedAt(Instant.parse("2026-03-11T00:00:00Z"))
                                .storedAt(Instant.parse("2026-03-11T00:00:00Z"))
                                .build()
                );

        AnalysisReportNarrativeGenerationFlowService service = new AnalysisReportNarrativeGenerationFlowService(
                analysisReportRepository,
                analysisLlmNarrativeGenerationService,
                new AnalysisReportNarrativeDraftFactory(objectMapper),
                analysisReportNarrativePersistenceService,
                new AnalysisLlmNarrativeProperties(true, "openai", "llm-prompt-v1", "llm-input-v1", "llm-output-v1"),
                new OpenAiProperties(true, "https://api.openai.com", "test-openai-key", "gpt-5.4", null, null, 5000, 30000),
                Clock.fixed(Instant.parse("2026-03-11T00:00:00Z"), ZoneOffset.UTC)
        );

        service.generateAndStoreLatest("BTCUSDT", AnalysisReportType.SHORT_TERM);

        ArgumentCaptor<AnalysisReportNarrativeDraft> draftCaptor =
                ArgumentCaptor.forClass(AnalysisReportNarrativeDraft.class);
        verify(analysisReportNarrativePersistenceService).save(draftCaptor.capture());
        assertThat(draftCaptor.getValue().llmProvider()).isEqualTo("openai");
        assertThat(draftCaptor.getValue().llmModel()).isEqualTo("gpt-5.4");
        assertThat(draftCaptor.getValue().promptTemplateVersion()).isEqualTo("llm-prompt-v1");
        assertThat(draftCaptor.getValue().inputSchemaVersion()).isEqualTo("llm-input-v1");
        assertThat(draftCaptor.getValue().outputSchemaVersion()).isEqualTo("llm-output-v1");
        assertThat(draftCaptor.getValue().requestedAt()).isEqualTo(Instant.parse("2026-03-11T00:00:00Z"));
    }
}

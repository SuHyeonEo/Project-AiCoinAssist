package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisLlmPromptCompositionReadServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisLlmNarrativeInputReadService analysisLlmNarrativeInputReadService;

    @Test
    void getLatestCompositionBuildsPromptFromLatestGptInput() {
        AnalysisLlmPromptCompositionReadService service = new AnalysisLlmPromptCompositionReadService(
                analysisLlmNarrativeInputReadService,
                new AnalysisLlmPromptComposer(new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules())
        );
        AnalysisLlmNarrativeInputPayload input = new AnalysisLlmNarrativeInputAssembler().assemble(
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                        .assemble(
                                reportEntity(
                                        AnalysisReportType.SHORT_TERM,
                                        java.time.Instant.parse("2026-03-09T00:59:59Z"),
                                        java.time.Instant.parse("2026-03-09T00:59:30Z"),
                                        "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                                        "gpt-5.4",
                                        "{\"summary\":\"unused\"}",
                                        java.time.Instant.parse("2026-03-09T01:00:30Z")
                                ),
                                shortTermPayload("Prompt summary")
                        )
        );

        when(analysisLlmNarrativeInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);

        AnalysisLlmPromptComposition composition = service.getLatestComposition("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(composition.systemPrompt()).contains("structured crypto market analysis writer");
        assertThat(composition.userPrompt()).contains("Prompt summary");
        assertThat(composition.outputSchemaJson()).contains("scenario_map");
    }
}

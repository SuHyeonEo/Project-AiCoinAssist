package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmPromptComposerTest extends AnalysisReportPayloadTestFixtures {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisLlmPromptComposer composer = new AnalysisLlmPromptComposer(objectMapper);
    private final AnalysisGptReportInputAssembler gptAssembler = new AnalysisGptReportInputAssembler(
            new AnalysisGptCrossSignalFactory()
    );
    private final AnalysisLlmNarrativeInputAssembler llmAssembler = new AnalysisLlmNarrativeInputAssembler();

    @Test
    void composeBuildsSystemPromptUserPromptAndOutputSchema() {
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        AnalysisLlmNarrativeInputPayload input = llmAssembler.assemble(
                gptAssembler.assemble(entity, shortTermPayload("Prompt summary"))
        );

        AnalysisLlmPromptComposition composition = composer.compose(
                input,
                List.of(new AnalysisLlmReferenceNewsItem(
                        "ETF delay headline",
                        "ExampleSource",
                        Instant.parse("2026-03-09T00:30:00Z"),
                        "https://example.com/news/1",
                        "May affect crypto risk appetite.",
                        "MACRO"
                ))
        );

        assertThat(composition.systemPrompt()).contains("You are a structured crypto market analysis writer.");
        assertThat(composition.systemPrompt()).contains("Do not provide investment advice");
        assertThat(composition.userPrompt()).contains("Length policy:");
        assertThat(composition.userPrompt()).contains("executive_conclusion");
        assertThat(composition.userPrompt()).contains("domain_analyses");
        assertThat(composition.userPrompt()).contains("Input JSON:");
        assertThat(composition.inputPayloadJson()).contains("\"metadata\"");
        assertThat(composition.inputPayloadJson()).contains("\"executive_summary\"");
        assertThat(composition.inputPayloadJson()).contains("\"optional_reference_news\"");
        assertThat(composition.outputSchemaJson()).contains("\"reference_news\"");
        assertThat(composition.outputLengthPolicyJson()).contains("\"executiveConclusionSummaryMaxChars\"");
        assertThat(composition.userPrompt()).contains("ETF delay headline");
    }
}

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
        assertThat(composition.systemPrompt()).contains("Write all explanatory text in Korean.");
        assertThat(composition.systemPrompt()).contains("Every interpretation must be tied to explicit input evidence");
        assertThat(composition.systemPrompt()).contains("Do not state unsupported conclusions");
        assertThat(composition.systemPrompt()).contains("do not copy enum-like labels such as MID_RANGE, NEUTRAL, CONTAINED");
        assertThat(composition.systemPrompt()).contains("Avoid meta-writing phrases such as");
        assertThat(composition.systemPrompt()).contains("설명했습니다");
        assertThat(composition.systemPrompt()).contains("All sentences must stay in -습니다 or -입니다 style");
        assertThat(composition.systemPrompt()).contains("Do not surface phrases such as \"Market state\"");
        assertThat(composition.userPrompt()).contains("Length policy:");
        assertThat(composition.userPrompt()).contains("must be written in Korean");
        assertThat(composition.userPrompt()).contains("Every supporting/risk factor must include its factual basis");
        assertThat(composition.userPrompt()).contains("MACD histogram is still negative");
        assertThat(composition.userPrompt()).contains("Support break risk 100 percent");
        assertThat(composition.userPrompt()).contains("converted to Asia/Seoul time");
        assertThat(composition.userPrompt()).contains("Do not repeat placeholder text such as YYYY/MM/DD HH:mm literally");
        assertThat(composition.userPrompt()).contains("Preferred sentence shape: 이 해설은 <actual Asia/Seoul time> 기준 데이터를 바탕으로 생성됐습니다.");
        assertThat(composition.userPrompt()).contains("MID_RANGE -> range middle zone");
        assertThat(composition.userPrompt()).contains("single calculated price level");
        assertThat(composition.userPrompt()).contains("단일 레벨 부근");
        assertThat(composition.userPrompt()).contains("server-calculated reference for confirmation");
        assertThat(composition.userPrompt()).contains("explicitly include the lower and upper bounds");
        assertThat(composition.userPrompt()).contains("server-calculated reference levels for monitoring");
        assertThat(composition.userPrompt()).contains("confirmation levels only");
        assertThat(composition.userPrompt()).contains("rewrite trigger and invalidation items into natural Korean");
        assertThat(composition.userPrompt()).contains("executive_conclusion");
        assertThat(composition.userPrompt()).contains("domain_analyses");
        assertThat(composition.userPrompt()).contains("Input JSON:");
        assertThat(composition.userPrompt()).contains("response format");
        assertThat(composition.userPrompt()).doesNotContain("Required output JSON schema:");
        assertThat(composition.userPrompt()).doesNotContain("Output length policy JSON:");
        assertThat(composition.inputPayloadJson()).contains("\"metadata\"");
        assertThat(composition.inputPayloadJson()).contains("\"executive_summary\"");
        assertThat(composition.inputPayloadJson()).contains("\"optional_reference_news\"");
        assertThat(composition.outputSchemaJson()).contains("\"reference_news\"");
        assertThat(composition.outputSchemaJson()).contains("\"additionalProperties\": false");
        assertThat(composition.outputLengthPolicyJson()).contains("\"executiveConclusionSummaryMaxChars\"");
        assertThat(composition.userPrompt()).contains("ETF delay headline");
    }
}

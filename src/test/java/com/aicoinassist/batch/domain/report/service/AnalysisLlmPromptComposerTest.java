package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmPromptComposerTest extends AnalysisReportPayloadTestFixtures {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisLlmPromptComposer composer = new AnalysisLlmPromptComposer(objectMapper);
    private final AnalysisGptReportInputAssembler gptAssembler = new AnalysisGptReportInputAssembler(
            new AnalysisGptCrossSignalFactory()
    );
    private final AnalysisLlmNarrativeInputAssembler llmAssembler = new AnalysisLlmNarrativeInputAssembler();

    @Test
    void composeBuildsUpdatedSchemaAndPromptGuidance() {
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

        AnalysisLlmPromptComposition composition = composer.compose(input);

        assertThat(composition.systemPrompt()).contains("structured report page");
        assertThat(composition.systemPrompt()).contains("News is excluded from this output");
        assertThat(composition.systemPrompt()).contains("Every sentence must end in expressions such as");
        assertThat(composition.systemPrompt()).contains("use natural connectors such as");
        assertThat(composition.userPrompt()).contains("hero_summary");
        assertThat(composition.userPrompt()).contains("domain_analyses should contain exactly 6 items");
        assertThat(composition.userPrompt()).contains("market_structure_box display values should stay within");
        assertThat(composition.userPrompt()).contains("market_structure_box labels should stay within");
        assertThat(composition.userPrompt()).contains("market_structure_box basis phrases should stay within");
        assertThat(composition.userPrompt()).contains("All fields in market_structure_box except interpretation are server-fixed display data");
        assertThat(composition.userPrompt()).contains("Copy range_low, current_price, range_high, range_position, upside_reference, downside_reference, support_break_risk, and resistance_break_risk from server_market_structure exactly");
        assertThat(composition.userPrompt()).contains("scenario_map should contain exactly 3 items");
        assertThat(composition.userPrompt()).contains("condition, trigger, confirmation, invalidation, and interpretation must all be complete polite Korean sentences");
        assertThat(composition.userPrompt()).contains("Use connective wording so related facts read as one report-style flow");
        assertThat(composition.userPrompt()).contains("bullish_factors must contain only supportive or constructive evidence");
        assertThat(composition.userPrompt()).contains("Do not place fear, headwind, elevated external risk");
        assertThat(composition.userPrompt()).contains("single calculated level");
        assertThat(composition.userPrompt()).contains("signal_headlines");
        assertThat(composition.userPrompt()).contains("primary_facts");
        assertThat(composition.userPrompt()).contains("market_structure_facts");
        assertThat(composition.userPrompt()).contains("derivative_structure_facts");
        assertThat(composition.userPrompt()).contains("macro_structure_facts");
        assertThat(composition.userPrompt()).contains("sentiment_structure_facts");
        assertThat(composition.userPrompt()).contains("onchain_structure_facts");
        assertThat(composition.userPrompt()).contains("external_structure_facts");
        assertThat(composition.userPrompt()).contains("level_structure_facts");
        assertThat(composition.userPrompt()).contains("scenario_guidance.confirmation_facts");
        assertThat(composition.userPrompt()).contains("BULLISH should require clearer upside expansion");
        assertThat(composition.userPrompt()).contains("Prefer market_structure_facts and primary_facts for the hero area");
        assertThat(composition.userPrompt()).contains("MARKET should primarily use market_structure_facts");
        assertThat(composition.userPrompt()).contains("For MACRO interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("For SENTIMENT interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("For ONCHAIN interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("Use this section as the report's explicit level and range structure box");
        assertThat(composition.userPrompt()).contains("cross_signal_integration to combine market_structure_facts");
        assertThat(composition.userPrompt()).contains("Use level_structure_facts and market_structure_facts for trigger and invalidation");
        assertThat(composition.userPrompt()).contains("Use server_market_structure as the source of truth for all fixed market_structure_box display fields");
        assertThat(composition.inputPayloadJson()).contains("\"metadata\"");
        assertThat(composition.inputPayloadJson()).contains("\"signal_headlines\"");
        assertThat(composition.inputPayloadJson()).contains("\"primary_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"market_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"derivative_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"macro_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"sentiment_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"onchain_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"external_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"server_market_structure\"");
        assertThat(composition.inputPayloadJson()).contains("\"level_structure_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"market_structure_box_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"scenario_guidance\"");
        assertThat(composition.inputPayloadJson()).doesNotContain("\"optional_reference_news\"");
        assertThat(composition.outputSchemaJson()).contains("\"hero_summary\"");
        assertThat(composition.outputSchemaJson()).contains("\"bullish_factors\"");
        assertThat(composition.outputSchemaJson()).contains("\"market_structure_box\"");
        assertThat(composition.outputSchemaJson()).contains("\"range_position\"");
        assertThat(composition.outputSchemaJson()).contains("\"upside_reference\"");
        assertThat(composition.outputSchemaJson()).contains("\"watch_point\"");
        assertThat(composition.outputSchemaJson()).contains("\"positioning_take\"");
        assertThat(composition.outputSchemaJson()).contains("\"scenario_type\"");
        assertThat(composition.outputSchemaJson()).doesNotContain("\"reference_news\"");
        assertThat(composition.outputLengthPolicyJson()).contains("\"heroSummaryMaxChars\"");
    }
}

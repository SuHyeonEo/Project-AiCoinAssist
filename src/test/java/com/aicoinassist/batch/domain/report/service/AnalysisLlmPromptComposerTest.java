package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextReference;
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
                gptAssembler.assemble(entity, shortTermPayload("Prompt summary")),
                sharedContextReference()
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
        assertThat(composition.userPrompt()).contains("use them to judge whether the current move has confirmation");
        assertThat(composition.userPrompt()).contains("quote-volume");
        assertThat(composition.userPrompt()).contains("taker-buy participation evidence");
        assertThat(composition.userPrompt()).contains("bullish_factors must contain only supportive or constructive evidence");
        assertThat(composition.userPrompt()).contains("Do not place fear, headwind, elevated external risk");
        assertThat(composition.userPrompt()).contains("single calculated level");
        assertThat(composition.userPrompt()).contains("signal_headlines");
        assertThat(composition.userPrompt()).contains("primary_facts");
        assertThat(composition.userPrompt()).contains("market_participation_facts");
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
        assertThat(composition.userPrompt()).contains("When shared_context_reference exists, treat it as the primary source for MACRO and SENTIMENT domain wording");
        assertThat(composition.userPrompt()).contains("MACRO should primarily use shared_context_reference.macro when present");
        assertThat(composition.userPrompt()).contains("SENTIMENT should primarily use shared_context_reference.sentiment when present");
        assertThat(composition.userPrompt()).contains("For MACRO interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("For SENTIMENT interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("For ONCHAIN interpretation, explicitly include at least one concrete metric or value");
        assertThat(composition.userPrompt()).contains("must not repeat the shared market summary in slightly different words");
        assertThat(composition.userPrompt()).contains("must explain only the asset-specific consequence or current-horizon implication");
        assertThat(composition.userPrompt()).contains("Use this section as the report's explicit level and range structure box");
        assertThat(composition.userPrompt()).contains("cross_signal_integration to combine market_structure_facts");
        assertThat(composition.userPrompt()).contains("Use level_structure_facts and market_structure_facts for trigger and invalidation");
        assertThat(composition.userPrompt()).contains("prefer them as breakout or breakdown validation evidence");
        assertThat(composition.userPrompt()).contains("Scenario interpretation must explain why that path matters for this asset and this horizon");
        assertThat(composition.userPrompt()).contains("Avoid generic template wording such as \"이 시나리오는 ... 의미합니다\"");
        assertThat(composition.userPrompt()).contains("Use shared_context_reference as already-written common macro and sentiment interpretation when it exists.");
        assertThat(composition.userPrompt()).contains("treat it as background context that has already been explained once");
        assertThat(composition.userPrompt()).contains("SHORT_TERM: prioritize immediate price structure, nearby support and resistance reaction");
        assertThat(composition.userPrompt()).contains("use volume, quote-volume, trade-count, and taker-buy confirmation aggressively");
        assertThat(composition.userPrompt()).contains("avoid writing as if one macro or sentiment datapoint defines the long-cycle trend");
        assertThat(composition.userPrompt()).contains("do not rewrite a second market-wide macro or sentiment essay");
        assertThat(composition.userPrompt()).contains("Prefer asset-specific consequences such as structure pressure, breakout difficulty, support fragility, leverage crowding, or on-chain confirmation");
        assertThat(composition.userPrompt()).contains("domain_analyses MACRO and SENTIMENT must stay focused on what that backdrop changes for this asset now");
        assertThat(composition.userPrompt()).contains("Use server_market_structure as the source of truth for all fixed market_structure_box display fields");
        assertThat(composition.inputPayloadJson()).contains("\"metadata\"");
        assertThat(composition.inputPayloadJson()).contains("\"shared_context_reference\"");
        assertThat(composition.inputPayloadJson()).contains("\"signal_headlines\"");
        assertThat(composition.inputPayloadJson()).contains("\"primary_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"market_participation_facts\"");
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

    private AnalysisLlmSharedContextReference sharedContextReference() {
        return new AnalysisLlmSharedContextReference(
                "shared-v1",
                "거시와 심리 공통 맥락은 혼조로 정리됩니다.",
                new AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "거시 조합은 뚜렷한 한 방향보다 혼조에 가깝습니다.",
                        "달러와 금리 흐름을 함께 확인할 필요가 있습니다."
                ),
                new AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "심리 지표는 아직 확신을 주지 못하고 있습니다.",
                        "심리 개선 여부를 추가로 확인할 필요가 있습니다."
                )
        );
    }
}

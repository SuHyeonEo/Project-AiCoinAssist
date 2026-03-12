package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputLengthPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptMetadata;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmRiskScenarioInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisLlmPromptComposer {

    private static final String SYSTEM_PROMPT = """
            You are a Korean crypto market analysis writer for a structured report page.

            Your role:
            - write only the narrative sections of the report
            - rely only on the provided server-calculated facts
            - do not calculate new metrics
            - do not invent missing numbers or events

            Hard rules:
            - Do not provide investment advice.
            - Do not predict the future as a certainty.
            - Do not invent any numeric fact, price level, trigger, or signal.
            - Use concise Korean research prose in natural polite formal style.
            - Every sentence must end in expressions such as "...습니다", "...입니다", "...보입니다", "...필요가 있습니다", or "...유의할 필요가 있습니다".
            - Do not use blunt note-style endings such as "...하다", "...이다", "...보인다", "...유효하다", or "...시사한다".
            - Do not sound like a memo, checklist, or internal note.
            - Rewrite enum-like English labels into natural Korean.
            - Avoid repeating the same point across sections.
            - When related facts are connected, use natural connectors such as "다만", "반면", "또한", "한편", "따라서", or "그러나" so the prose flows like a report instead of isolated notes.
            - Use only JSON and follow the schema exactly.
            - Do not include markdown fences.
            - News is excluded from this output.
            """;

    private static final String OUTPUT_SCHEMA_JSON = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "hero_summary",
                "executive_conclusion",
                "domain_analyses",
                "market_structure_box",
                "cross_signal_integration",
                "scenario_map"
              ],
              "properties": {
                "hero_summary": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["market_regime", "one_line_take", "primary_driver", "risk_driver"],
                  "properties": {
                    "market_regime": { "type": "string" },
                    "one_line_take": { "type": "string" },
                    "primary_driver": { "type": "string" },
                    "risk_driver": { "type": "string" }
                  }
                },
                "executive_conclusion": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["summary", "bullish_factors", "bearish_factors", "tactical_view"],
                  "properties": {
                    "summary": { "type": "string" },
                    "bullish_factors": {
                      "type": "array",
                      "minItems": 3,
                      "maxItems": 3,
                      "items": { "type": "string" }
                    },
                    "bearish_factors": {
                      "type": "array",
                      "minItems": 3,
                      "maxItems": 3,
                      "items": { "type": "string" }
                    },
                    "tactical_view": { "type": "string" }
                  }
                },
                "domain_analyses": {
                  "type": "array",
                  "minItems": 6,
                  "maxItems": 6,
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": ["domain", "status", "interpretation", "watch_point"],
                    "properties": {
                      "domain": {
                        "type": "string",
                        "enum": ["MARKET", "DERIVATIVE", "MACRO", "SENTIMENT", "ONCHAIN", "LEVEL"]
                      },
                      "status": {
                        "type": "string",
                        "enum": ["BULLISH", "NEUTRAL", "BEARISH", "MIXED"]
                      },
                      "interpretation": { "type": "string" },
                      "watch_point": { "type": "string" }
                    }
                  }
                },
                "market_structure_box": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": [
                    "range_low",
                    "current_price",
                    "range_high",
                    "range_position",
                    "upside_reference",
                    "downside_reference",
                    "support_break_risk",
                    "resistance_break_risk",
                    "interpretation"
                  ],
                  "properties": {
                    "range_low": { "type": "string" },
                    "current_price": { "type": "string" },
                    "range_high": { "type": "string" },
                    "range_position": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["value", "label", "basis"],
                      "properties": {
                        "value": { "type": "string" },
                        "label": { "type": "string" },
                        "basis": { "type": "string" }
                      }
                    },
                    "upside_reference": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["value", "label", "basis"],
                      "properties": {
                        "value": { "type": "string" },
                        "label": { "type": "string" },
                        "basis": { "type": "string" }
                      }
                    },
                    "downside_reference": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["value", "label", "basis"],
                      "properties": {
                        "value": { "type": "string" },
                        "label": { "type": "string" },
                        "basis": { "type": "string" }
                      }
                    },
                    "support_break_risk": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["value", "label", "basis"],
                      "properties": {
                        "value": { "type": "string" },
                        "label": { "type": "string" },
                        "basis": { "type": "string" }
                      }
                    },
                    "resistance_break_risk": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["value", "label", "basis"],
                      "properties": {
                        "value": { "type": "string" },
                        "label": { "type": "string" },
                        "basis": { "type": "string" }
                      }
                    },
                    "interpretation": { "type": "string" }
                  }
                },
                "cross_signal_integration": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["alignment_summary", "dominant_drivers", "conflict_summary", "positioning_take"],
                  "properties": {
                    "alignment_summary": { "type": "string" },
                    "dominant_drivers": {
                      "type": "array",
                      "minItems": 2,
                      "maxItems": 4,
                      "items": { "type": "string" }
                    },
                    "conflict_summary": { "type": "string" },
                    "positioning_take": { "type": "string" }
                  }
                },
                "scenario_map": {
                  "type": "array",
                  "minItems": 3,
                  "maxItems": 3,
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": [
                      "scenario_type",
                      "title",
                      "condition",
                      "trigger",
                      "confirmation",
                      "invalidation",
                      "interpretation"
                    ],
                    "properties": {
                      "scenario_type": {
                        "type": "string",
                        "enum": ["BULLISH", "BASE", "BEARISH"]
                      },
                      "title": { "type": "string" },
                      "condition": { "type": "string" },
                      "trigger": { "type": "string" },
                      "confirmation": { "type": "string" },
                      "invalidation": { "type": "string" },
                      "interpretation": { "type": "string" }
                    }
                  }
                }
              }
            }
            """;

    private final ObjectMapper objectMapper;

    public AnalysisLlmPromptComposition compose(AnalysisLlmNarrativeInputPayload input) {
        AnalysisLlmOutputLengthPolicy lengthPolicy = AnalysisLlmOutputLengthPolicy.defaultPolicy();
        AnalysisLlmPromptInputPayload promptInput = new AnalysisLlmPromptInputPayload(
                new AnalysisLlmPromptMetadata(
                        input.symbol(),
                        input.reportType(),
                        input.analysisBasisTime(),
                        input.rawReferenceTime(),
                        input.sourceDataVersion(),
                        input.analysisEngineVersion()
                ),
                input.sharedContextReference(),
                input.executiveSummary(),
                input.signalHeadlines(),
                input.primaryFacts(),
                input.marketStructureFacts(),
                input.derivativeStructureFacts(),
                input.macroStructureFacts(),
                input.sentimentStructureFacts(),
                input.onchainStructureFacts(),
                input.externalStructureFacts(),
                input.serverMarketStructure(),
                input.levelStructureFacts(),
                input.marketStructureBoxFacts(),
                input.domainFactBlocks(),
                input.crossSignals(),
                input.scenarioGuidance(),
                new AnalysisLlmRiskScenarioInput(
                        input.riskFactors(),
                        input.scenarios(),
                        input.continuityNotes()
                )
        );

        String inputJson = serialize(promptInput);
        String lengthPolicyJson = serialize(lengthPolicy);
        String userPrompt = """
                Generate output in the required JSON schema.
                All narrative text must be written in Korean.

                Length policy:
                - hero_summary fields should stay within %d characters each.
                - executive_conclusion.summary should stay within %d characters.
                - bullish_factors and bearish_factors should stay within %d items each and each item within %d characters.
                - tactical_view should stay within %d characters.
                - domain_analyses should contain exactly 6 items.
                - domain interpretation should stay within %d characters.
                - domain watch_point should stay within %d characters.
                - market_structure_box display values should stay within %d characters each.
                - market_structure_box labels should stay within %d characters each.
                - market_structure_box basis phrases should stay within %d characters each.
                - market_structure_box.interpretation should stay within %d characters.
                - cross_signal_integration summaries should stay within %d characters.
                - dominant_drivers should stay within %d items and each item within %d characters.
                - positioning_take should stay within %d characters.
                - scenario_map should contain exactly 3 items: BULLISH, BASE, BEARISH.
                - scenario title should stay within %d characters.
                - scenario condition, trigger, confirmation, and invalidation should each stay within %d characters.
                - scenario interpretation should stay within %d characters.

                Section requirements:

                1) hero_summary
                - Write four short lines suitable for the report hero area.
                - Every field must be a natural polite Korean sentence, not a noun fragment.
                - When two related facts need to be connected, prefer one smooth sentence with a light connector instead of two disconnected fragments.
                - market_regime should summarize the current market regime.
                - one_line_take should compress the main conclusion into one sentence.
                - primary_driver should name the strongest current driver.
                - risk_driver should name the most important current risk.
                - Prefer market_structure_facts and primary_facts for the hero area.
                - Use derivative_structure_facts, external_structure_facts, or level_structure_facts only when they clearly dominate the current short conclusion.
                - The hero area should sound like the opening lines of a report page, not a list of metrics.

                2) executive_conclusion
                - summary should integrate the main structured facts without repeating every domain card.
                - bullish_factors and bearish_factors should be concise, evidence-based, and anchored to at least one explicit fact from the input.
                - Avoid generic phrases that could be written without seeing the input.
                - tactical_view should explain the current stance in a restrained way.
                - Do not use forecast language or target-price wording.
                - Use connective wording so related facts read as one report-style flow, not as isolated short clauses.
                - Prefer primary_facts first, then add the most decision-relevant support from market_structure_facts, derivative_structure_facts, external_structure_facts, and level_structure_facts.
                - bullish_factors and bearish_factors should read like editorial bullets from a crypto report page, not raw metric fragments.
                - Each factor should combine one fact with its meaning, for example metric plus why that metric matters now.
                - bullish_factors must contain only supportive or constructive evidence for the current horizon.
                - bearish_factors must contain only downside, restrictive, or cautionary evidence for the current horizon.
                - Do not place fear, headwind, elevated external risk, defensive positioning, downside break risk, or similar cautionary evidence inside bullish_factors.
                - Do not place supportive trend structure, positive momentum, constructive positioning, or similar supportive evidence inside bearish_factors unless the sentence explicitly explains why that support is weakening.

                3) domain_analyses
                - Cover the six domains MARKET, DERIVATIVE, MACRO, SENTIMENT, ONCHAIN, LEVEL.
                - status must be one of BULLISH, NEUTRAL, BEARISH, MIXED.
                - interpretation should explain what the current facts mean in analyst-style Korean prose.
                - watch_point should state the next thing to monitor.
                - When level input points to a single calculated level, describe it as a confirmation reference level, not a forecast.
                - LEVEL interpretation should explain current price location versus nearby support/resistance or active zone, what holding would imply, and what breaking would imply.
                - Within interpretation, connect related evidence with restrained connective words so the sentence reads smoothly.
                - Prefer structure-aware wording such as "지지 확인", "저항 재확인", "상단 돌파 시도", "하단 이탈 시 구조 약화", or "레인지 상단 부담".
                - When shared_context_reference exists, treat it as the primary source for MACRO and SENTIMENT domain wording before looking at raw fact lists.
                - MARKET should primarily use market_structure_facts.
                - DERIVATIVE should primarily use derivative_structure_facts.
                - MACRO should primarily use shared_context_reference.macro when present, otherwise use macro_structure_facts.
                - SENTIMENT should primarily use shared_context_reference.sentiment when present, otherwise use sentiment_structure_facts.
                - ONCHAIN should primarily use onchain_structure_facts.
                - LEVEL should primarily use level_structure_facts.
                - Use domain_fact_blocks as the domain baseline, and use structure_facts to add the sharper evidence that makes the prose feel concrete.
                - For each domain, avoid listing more than two factual anchors inside one interpretation sentence.
                - For MACRO interpretation, explicitly include at least one concrete metric or value such as DXY, US10Y, USD/KRW, or a deviation fact when available.
                - For SENTIMENT interpretation, explicitly include at least one concrete metric or value such as Fear & Greed value, classification, or greed/fear sample balance when available.
                - For ONCHAIN interpretation, explicitly include at least one concrete metric or value such as active addresses, transactions, market cap, or a deviation fact when available.
                - If numeric evidence exists for MACRO, SENTIMENT, or ONCHAIN, do not write those interpretations as fully generic prose.

                4) market_structure_box
                - Use this section as the report's explicit level and range structure box.
                - All fields in market_structure_box except interpretation are server-fixed display data.
                - Copy range_low, current_price, range_high, range_position, upside_reference, downside_reference, support_break_risk, and resistance_break_risk from server_market_structure exactly.
                - Do not reinterpret, replace, recalculate, or paraphrase those eight fields.
                - interpretation should be the only field in this section that you newly write.
                - interpretation should be one short Korean report paragraph that ties the server-fixed range and level facts together.
                - Prefer server_market_structure first, then use level_structure_facts and market_structure_facts only to explain why the current structure matters.
                - Do not turn interpretation into a second list of raw numbers.
                - This section should resemble a visual level and structure card that the API or frontend can assemble directly.

                5) cross_signal_integration
                - alignment_summary should explain which domains are broadly aligned and in what direction.
                - dominant_drivers should list the main drivers only.
                - conflict_summary should explain which domains or factors are pulling against that alignment.
                - positioning_take should give a restrained positioning interpretation and must not simply restate the alignment summary.
                - Prefer cross_signal_integration to combine market_structure_facts, derivative_structure_facts, external_structure_facts, and level_structure_facts.
                - This section should feel like the report's synthesis layer: which facts point the same way, which facts resist that direction, and what that means for the current horizon.
                - Make the synthesis read as linked report prose by using connectors where appropriate, especially when contrast or limitation matters.
                - Do not turn this section into another domain summary list.

                6) scenario_map
                - Return exactly three scenarios with scenario_type BULLISH, BASE, BEARISH.
                - Every scenario must be conditional, not predictive.
                - condition, trigger, confirmation, invalidation, and interpretation must all be complete polite Korean sentences.
                - trigger, confirmation, and invalidation must rely on explicit structured facts from the input.
                - interpretation should explain what the scenario would mean, not what will happen.
                - BULLISH, BASE, and BEARISH must represent materially different paths.
                - BASE should describe the most likely continuation or range-maintenance path under the current structure.
                - BULLISH should require clearer upside expansion or breakout conditions than BASE.
                - BEARISH should require explicit deterioration, breakdown, or downside pressure conditions.
                - Do not reuse near-identical wording across the three scenarios.
                - Use connectors to show flow between setup, trigger, confirmation, and invalidation rather than writing clipped note-style fragments.
                - Prefer scenario_guidance.confirmation_facts first for confirmation.
                - Use level_structure_facts and market_structure_facts for trigger and invalidation when the scenario depends on structure holding or breaking.
                - Use derivative_structure_facts and external_structure_facts when leverage pressure or outside regime pressure materially affects the path.
                - Each scenario should read like a report card: current setup, what would push it forward, what would confirm it, and what would cancel it.

                Input fact usage guidance:
                - Use signal_headlines to identify the report's most visible top-line signals.
                - Use shared_context_reference as already-written common macro and sentiment interpretation when it exists.
                - When shared_context_reference exists, treat it as background context that has already been explained once, and spend new tokens on this asset's chart, derivative, on-chain, and level implications.
                - Horizon interpretation lens:
                %s
                - Use primary_facts as high-priority evidence that should appear in executive_conclusion or cross_signal_integration.
                - Use market_structure_facts for moving-average arrangement, momentum, and current structural position.
                - Use derivative_structure_facts for funding versus average, OI versus average, basis expansion or contraction, and price/OI alignment facts.
                - Use macro_structure_facts for DXY, US10Y, USD/KRW deviation and risk-on or risk-off pressure facts.
                - Use sentiment_structure_facts for Fear & Greed deviation, classification shift, and greed/fear balance facts.
                - Use onchain_structure_facts for active-address, transaction, and market-cap participation facts.
                - Use external_structure_facts for dominant risk direction, composite risk score, persistence, and reversal-risk facts.
                - Use level_structure_facts for zone location, nearest level distance, and repeated test/break facts.
                - Use server_market_structure as the source of truth for all fixed market_structure_box display fields.
                - Use market_structure_box_facts only as supplementary background when writing market_structure_box.interpretation.
                - Use domain_fact_blocks for domain-specific interpretation, and prefer facts with concrete numbers, deltas, ranges, moving-average positions, momentum values, and nearest level details when available.
                - When shared_context_reference exists, do not restate it verbatim across sections. Use it as shared context and focus the new writing on how that context matters for this asset and horizon.
                - When shared_context_reference exists, do not rewrite a second market-wide macro or sentiment essay. Translate that shared backdrop into this asset's specific implication instead.
                - Prefer asset-specific consequences such as structure pressure, breakout difficulty, support fragility, leverage crowding, or on-chain confirmation instead of repeating the shared market summary itself.
                - If a macro or sentiment point is already fully expressed in shared_context_reference, only mention it again when you connect it to a concrete asset fact from market_structure_facts, derivative_structure_facts, onchain_structure_facts, or level_structure_facts.
                - Use market_structure_box for explicit upper and lower reference explanation, and avoid hiding key support or resistance references only inside scenario_map.
                - Use scenario_guidance.confirmation_facts as confirmation candidates, and do not replace them with invented signals.
                - Use limited_risks_scenarios for scenario conditions, invalidation, and restrained tactical meaning.
                - If a desirable fact such as level distance, moving-average arrangement, momentum detail, derivative crowding, or scenario confirmation is missing, stay restrained and say less rather than inventing it.
                - Do not spread every fact everywhere. Select the fact group that best fits the section's role.
                - Prefer concrete metric-backed facts in executive_conclusion and domain_analyses, and more integrated meaning-focused prose in hero_summary and cross_signal_integration.

                Section separation rule:
                - Do not repeat the same claim across hero_summary, executive_conclusion, domain_analyses, cross_signal_integration, and scenario_map.
                - Each section must contribute a different layer of interpretation.
                - If a point was already made in a previous section, the next section must deepen it or connect it, not restate it.

                Input JSON:
                %s
                """.formatted(
                lengthPolicy.heroSummaryMaxChars(),
                lengthPolicy.executiveConclusionSummaryMaxChars(),
                lengthPolicy.executiveConclusionFactorMaxItems(),
                lengthPolicy.executiveConclusionFactorItemMaxChars(),
                lengthPolicy.executiveConclusionTacticalViewMaxChars(),
                lengthPolicy.domainInterpretationMaxChars(),
                lengthPolicy.domainWatchPointMaxChars(),
                lengthPolicy.marketStructureValueMaxChars(),
                lengthPolicy.marketStructureLabelMaxChars(),
                lengthPolicy.marketStructureBasisMaxChars(),
                lengthPolicy.marketStructureInterpretationMaxChars(),
                lengthPolicy.crossSignalSummaryMaxChars(),
                lengthPolicy.crossSignalListMaxItems(),
                lengthPolicy.crossSignalItemMaxChars(),
                lengthPolicy.crossSignalPositioningTakeMaxChars(),
                lengthPolicy.scenarioTitleMaxChars(),
                lengthPolicy.scenarioFieldMaxChars(),
                lengthPolicy.scenarioInterpretationMaxChars(),
                horizonGuidance(input),
                inputJson
        );

        return new AnalysisLlmPromptComposition(
                SYSTEM_PROMPT,
                userPrompt,
                inputJson,
                OUTPUT_SCHEMA_JSON,
                lengthPolicyJson
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize LLM prompt input.", exception);
        }
    }

    private String horizonGuidance(AnalysisLlmNarrativeInputPayload input) {
        if (input == null || input.reportType() == null) {
            return "- Use a balanced interpretation lens and stay conservative when horizon-specific guidance is not available.";
        }
        return switch (input.reportType()) {
            case SHORT_TERM -> """
                    - SHORT_TERM: prioritize immediate price structure, nearby support and resistance reaction, short-term momentum continuation or slowdown, and whether shared macro or sentiment pressure changes the next move.
                    - SHORT_TERM: avoid writing as if one macro or sentiment datapoint defines the long-cycle trend.
                    """.strip();
            case MID_TERM -> """
                    - MID_TERM: prioritize whether the current structure can persist across days to weeks, whether the backdrop supports consolidation or continuation, and whether pressure is accumulating against the current trend.
                    - MID_TERM: connect shared macro or sentiment context to structural durability rather than only the next candle move.
                    """.strip();
            case LONG_TERM -> """
                    - LONG_TERM: prioritize cycle position, durable regime backdrop, long-duration support and resistance tolerance, and whether the asset is operating in a supportive or restrictive long-horizon environment.
                    - LONG_TERM: avoid overreacting to short-lived noise and explain shared macro or sentiment context as a broad environment rather than a near-term trigger.
                    """.strip();
        };
    }
}

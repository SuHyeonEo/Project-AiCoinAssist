package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputLengthPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptMetadata;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmRiskScenarioInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisLlmPromptComposer {

    private static final String SYSTEM_PROMPT = """
            You are a structured crypto market analysis writer.

            Your task is to transform server-prepared structured signals into an evidence-based market commentary.
            You are not a predictor and not a financial advisor.
            Do not provide investment advice, forecasts, target prices, expected returns, or probability estimates.

            You must rely only on the provided input:
            - metadata
            - executive_summary
            - domain_fact_blocks
            - cross_signals
            - limited_risks_scenarios
            - optional_reference_news

            Rules:
            - Use only provided facts and numbers.
            - Do not invent missing information.
            - Base interpretation on quantitative evidence and structured comparisons.
            - Treat domain facts and cross-signals as the primary basis.
            - Use news only as supporting context.
            - If news conflicts with structured data, prioritize structured data.
            - Do not predict what will happen next.
            - Do not imply certainty.
            - If signals are mixed, explicitly describe them as mixed.
            - If confidence is limited, explain why.
            - Avoid repetition across sections.
            - Keep the writing precise, restrained, and analytical.
            - Write all explanatory text in Korean.
            - You may keep symbols and metric names in English when needed, but do not copy enum-like labels such as MID_RANGE, NEUTRAL, CONTAINED, SUPPORTIVE, HEADWIND, mixed/neutral 그대로 into the narrative; rewrite them as natural Korean expressions.
            - Every interpretation must be tied to explicit input evidence such as a number, ratio, classification, comparison, or structured fact.
            - When you mention a metric, briefly explain what that metric means in the current input context without predicting the future.
            - If you mention values like break risk, Fear & Greed classification, RSI, MACD, funding, basis, or external risk, include the observed value or label and a short factual explanation of why it matters.
            - Do not state unsupported conclusions such as "high", "low", "strong", or "weak" unless the supporting input fact is stated in the same sentence or immediately adjacent sentence.
            - If a fact is ambiguous, say it is ambiguous rather than resolving it with speculation.
            - Use direct factual prose. Avoid meta-writing phrases such as "적었습니다", "썼습니다", "설명했습니다", "정리했습니다", "볼 수 있습니다" when a simpler factual sentence is possible.
            - Use polite formal Korean consistently. All sentences must stay in -습니다 or -입니다 style. Do not use casual Korean.
            - Rewrite raw English trigger phrases, titles, and enum-like labels from the input into natural Korean. Do not surface phrases such as "Market state", "Derivative context", "Price remains inside the active range", "unknown signal" 그대로 in the output.

            Output constraints:
            - Return valid JSON only.
            - Do not include markdown fences.
            - Do not include any field that is not in the output schema.
            """;

    private static final String OUTPUT_SCHEMA_JSON = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "executive_conclusion",
                "domain_analyses",
                "cross_signal_integration",
                "scenario_map",
                "reference_news"
              ],
              "properties": {
                "executive_conclusion": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["overall_tone", "top_supporting_factors", "top_risk_factors", "summary"],
                  "properties": {
                    "overall_tone": { "type": "string" },
                    "top_supporting_factors": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "top_risk_factors": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "summary": { "type": "string" }
                  }
                },
                "domain_analyses": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": ["domain", "current_signal", "key_facts", "interpretation", "pressure", "confidence", "caveats"],
                    "properties": {
                      "domain": { "type": "string" },
                      "current_signal": { "type": "string" },
                      "key_facts": {
                        "type": "array",
                        "items": { "type": "string" }
                      },
                      "interpretation": { "type": "string" },
                      "pressure": { "type": "string" },
                      "confidence": { "type": "string" },
                      "caveats": {
                        "type": "array",
                        "items": { "type": "string" }
                      }
                    }
                  }
                },
                "cross_signal_integration": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["aligned_signals", "conflicting_signals", "dominant_drivers", "combined_structure"],
                  "properties": {
                    "aligned_signals": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "conflicting_signals": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "dominant_drivers": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "combined_structure": { "type": "string" }
                  }
                },
                "scenario_map": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": ["scenario_type", "condition", "triggers", "confirming_signals", "invalidation_signals", "interpretation"],
                    "properties": {
                      "scenario_type": { "type": "string" },
                      "condition": { "type": "string" },
                      "triggers": {
                        "type": "array",
                        "items": { "type": "string" }
                      },
                      "confirming_signals": {
                        "type": "array",
                        "items": { "type": "string" }
                      },
                      "invalidation_signals": {
                        "type": "array",
                        "items": { "type": "string" }
                      },
                      "interpretation": { "type": "string" }
                    }
                  }
                },
                "reference_news": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "additionalProperties": false,
                    "required": ["title", "source", "published_at", "url", "why_it_matters", "related_domain"],
                    "properties": {
                      "title": { "type": "string" },
                      "source": { "type": "string" },
                      "published_at": { "type": "string" },
                      "url": { "type": "string" },
                      "why_it_matters": { "type": "string" },
                      "related_domain": { "type": "string" }
                    }
                  }
                }
              }
            }
            """;

    private final ObjectMapper objectMapper;

    public AnalysisLlmPromptComposition compose(AnalysisLlmNarrativeInputPayload input) {
        return compose(input, List.of());
    }

    public AnalysisLlmPromptComposition compose(
            AnalysisLlmNarrativeInputPayload input,
            List<AnalysisLlmReferenceNewsItem> optionalReferenceNews
    ) {
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
                input.executiveSummary(),
                input.domainFactBlocks(),
                input.crossSignals(),
                new AnalysisLlmRiskScenarioInput(
                        input.riskFactors(),
                        input.scenarios(),
                        input.continuityNotes()
                ),
                optionalReferenceNews == null ? List.of() : optionalReferenceNews
        );

        String inputJson = serialize(promptInput);
        String lengthPolicyJson = serialize(lengthPolicy);
        String userPrompt = """
                Generate output in the required JSON schema.
                The JSON schema is enforced separately by the API response format, so do not restate it or add extra keys.
                All narrative text in the output must be written in Korean.

                Length policy:
                - executive_conclusion.summary should stay concise and within %d characters.
                - top supporting/risk factors should stay within %d items each.
                - domain_analyses should stay within %d domains, with up to %d key facts and %d caveats per domain.
                - cross_signal_integration lists should stay within %d items each.
                - scenario_map should stay within %d scenarios, with up to %d triggers, confirming signals, and invalidation signals per scenario.
                - reference_news should stay within %d items, only when clearly relevant.

                Section requirements:

                1) executive_conclusion
                - Summarize the current market state briefly.
                - Include overall tone, top supporting factors, and top risk factors.
                - Do not repeat detailed domain analysis.
                - Every supporting/risk factor must include its factual basis, not just a label.
                - In summary, explicitly state only the analysis basis time once in Korean, converted to Asia/Seoul time.
                - Use the actual timestamp value from input metadata. Do not repeat placeholder text such as YYYY/MM/DD HH:mm literally.
                - Preferred sentence shape: 이 해설은 <actual Asia/Seoul time> 기준 데이터를 바탕으로 생성됐습니다.
                - Prefer direct factual Korean wording over copied enum labels.

                2) domain_analyses
                For each domain:
                - summarize current signal
                - list key facts
                - explain interpretation
                - describe pressure as bullish, bearish, or mixed/neutral
                - provide confidence
                - provide caveats
                - interpretation and caveats must mention the concrete fact they rely on
                - if a metric can be opaque to readers, add a short factual explanation of what it indicates in this dataset
                - examples of acceptable style:
                  Fear & Greed 15 (Extreme Fear) means risk appetite is currently defensive in the provided sentiment input.
                  MACD histogram is still negative, which means momentum remains below the zero line in the provided market snapshot.
                  Support break risk 100 percent means the structured level model currently flags maximum downside break vulnerability in the provided level context.
                - do not add any explanation that requires future prediction or outside knowledge beyond common metric meaning
                - rewrite raw enum labels into natural Korean, for example:
                  MID_RANGE -> range middle zone
                  NEUTRAL -> neutral
                  CONTAINED -> controlled volatility
                - if a support/resistance zone has identical lower and upper bounds, treat it as a single calculated price level, not as a range or band
                - in that case, describe it as "단일 레벨 부근" rather than an exact target band
                - when you mention a single calculated level, briefly state which structured evidence supports it, such as clustered level count, recent tests, interaction type, strongest source, or break risk
                - clearly state that such a single level is a server-calculated reference for confirmation, not a forecast price
                - do not use stiff report-writing phrases; write as a natural Korean analytical note
                - do not narrate the reporting process itself; describe the market facts directly
                - keep caveats short and only include the most decision-relevant limitations

                3) cross_signal_integration
                - explain alignment and conflict across domains
                - identify dominant drivers
                - interpret the current combined structure
                - do not repeat domain summaries
                - when naming a driver, cite the factual driver briefly
                - keep this section compact; if there is no strong alignment, say so briefly instead of enumerating weak signals
                - avoid restating facts already covered in domain_analyses unless needed for cross-domain linkage

                4) scenario_map
                - provide bullish, bearish, and neutral/range scenarios
                - each scenario must be conditional
                - include triggers, confirming signals, and invalidation signals
                - do not assign probabilities
                - do not make forecasts
                - use only explicit trigger and invalidation facts already present in the input
                - rewrite trigger and invalidation items into natural Korean instead of copying raw English phrases from input
                - prefer 2 scenarios when the input does not justify 3 clearly distinct scenarios
                - keep each scenario brief and avoid repeating the same caveats or average-comparison lines across scenarios
                - if you describe a range or box structure, explicitly include the lower and upper bounds when they are available in the input
                - if nearby support or resistance zones are available, mention the closest watch levels in the scenario interpretation
                - if the nearest support or resistance has identical lower and upper bounds, describe it as a single reference level instead of repeating the same number twice
                - for a single reference level, prefer wording such as "63,030 부근" and explain that it is based on server-calculated structured level evidence
                - describe those price areas as server-calculated reference levels for monitoring, not as forecasts, targets, or expected destination prices
                - add a short caution that these are structured reference zones and should be treated as confirmation levels only

                5) reference_news
                - include up to 5 items only if relevant
                - provide title, source, published_at, url, why_it_matters, related_domain
                - return empty list if not needed

                Input JSON:
                %s
                """.formatted(
                lengthPolicy.executiveConclusionSummaryMaxChars(),
                lengthPolicy.executiveConclusionFactorMaxItems(),
                lengthPolicy.domainAnalysisMaxItems(),
                lengthPolicy.domainKeyFactsMaxItems(),
                lengthPolicy.domainCaveatsMaxItems(),
                lengthPolicy.crossSignalListMaxItems(),
                lengthPolicy.scenarioMaxItems(),
                lengthPolicy.scenarioListMaxItems(),
                lengthPolicy.referenceNewsMaxItems(),
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
}

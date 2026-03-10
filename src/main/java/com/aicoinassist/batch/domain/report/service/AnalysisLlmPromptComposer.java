package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
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

            Output constraints:
            - Return valid JSON only.
            - Do not include markdown fences.
            - Do not include any field that is not in the output schema.
            """;

    private static final String OUTPUT_SCHEMA_JSON = """
            {
              "type": "object",
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
                  "required": ["overall_tone", "top_supporting_factors", "top_risk_factors", "summary"]
                },
                "domain_analyses": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "required": ["domain", "current_signal", "key_facts", "interpretation", "pressure", "confidence", "caveats"]
                  }
                },
                "cross_signal_integration": {
                  "type": "object",
                  "required": ["aligned_signals", "conflicting_signals", "dominant_drivers", "combined_structure"]
                },
                "scenario_map": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "required": ["scenario_type", "condition", "triggers", "confirming_signals", "invalidation_signals", "interpretation"]
                  }
                },
                "reference_news": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "required": ["title", "source", "published_at", "url", "why_it_matters", "related_domain"]
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
        String userPrompt = """
                Generate output in the required JSON schema.

                Section requirements:

                1) executive_conclusion
                - Summarize the current market state briefly.
                - Include overall tone, top supporting factors, and top risk factors.
                - Do not repeat detailed domain analysis.

                2) domain_analyses
                For each domain:
                - summarize current signal
                - list key facts
                - explain interpretation
                - describe pressure as bullish, bearish, or mixed/neutral
                - provide confidence
                - provide caveats

                3) cross_signal_integration
                - explain alignment and conflict across domains
                - identify dominant drivers
                - interpret the current combined structure
                - do not repeat domain summaries

                4) scenario_map
                - provide bullish, bearish, and neutral/range scenarios
                - each scenario must be conditional
                - include triggers, confirming signals, and invalidation signals
                - do not assign probabilities
                - do not make forecasts

                5) reference_news
                - include up to 5 items only if relevant
                - provide title, source, published_at, url, why_it_matters, related_domain
                - return empty list if not needed

                Input JSON:
                %s

                Required output JSON schema:
                %s
                """.formatted(inputJson, OUTPUT_SCHEMA_JSON);

        return new AnalysisLlmPromptComposition(
                SYSTEM_PROMPT,
                userPrompt,
                inputJson,
                OUTPUT_SCHEMA_JSON
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize LLM prompt input.", exception);
        }
    }
}

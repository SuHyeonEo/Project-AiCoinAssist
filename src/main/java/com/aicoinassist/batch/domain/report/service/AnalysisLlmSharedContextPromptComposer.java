package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisLlmSharedContextPromptComposer {

    private static final String SYSTEM_PROMPT = """
            You are a Korean crypto market context writer.

            Your role:
            - summarize only the shared macro and sentiment context
            - write concise Korean research prose
            - do not mention any asset-specific chart, level, derivative, or on-chain fact
            - do not invent missing numbers or events

            Hard rules:
            - Use only JSON and follow the schema exactly.
            - Every sentence must be written in natural polite Korean prose.
            - Every sentence must end in expressions such as "...습니다", "...입니다", "...보입니다", or "...필요가 있습니다".
            - Do not use blunt note-style endings such as "...하다", "...이다", or "...보인다".
            - Rewrite enum-like English labels into natural Korean.
            - Avoid repeating the same point across shared_summary, macro.summary, and sentiment.summary.
            - Use natural connectors such as "다만", "반면", "또한", "한편", "따라서", or "그러나" when contrast matters.
            - Do not include markdown fences.
            """;

    private static final String OUTPUT_SCHEMA_JSON = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": ["shared_summary", "macro", "sentiment"],
              "properties": {
                "shared_summary": { "type": "string" },
                "macro": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["status", "summary", "watch_point"],
                  "properties": {
                    "status": { "type": "string", "enum": ["BULLISH", "NEUTRAL", "BEARISH", "MIXED"] },
                    "summary": { "type": "string" },
                    "watch_point": { "type": "string" }
                  }
                },
                "sentiment": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["status", "summary", "watch_point"],
                  "properties": {
                    "status": { "type": "string", "enum": ["BULLISH", "NEUTRAL", "BEARISH", "MIXED"] },
                    "summary": { "type": "string" },
                    "watch_point": { "type": "string" }
                  }
                }
              }
            }
            """;

    private final ObjectMapper objectMapper;

    public AnalysisLlmPromptComposition compose(AnalysisLlmSharedContextInputPayload input) {
        String inputJson = serialize(input);
        String userPrompt = """
                Generate a shared context reference for one report horizon.

                Horizon interpretation lens:
                %s

                Requirements:
                - shared_summary should summarize the macro and sentiment backdrop in one short Korean paragraph.
                - macro should explain only the shared macro context for this horizon.
                - sentiment should explain only the shared sentiment context for this horizon.
                - Do not mention BTC, ETH, XRP, support, resistance, chart pattern, derivative positioning, or on-chain activity.
                - Do not restate every fact line-by-line. Compress the meaning into editorial Korean prose.
                - shared_summary should act as a market-wide backdrop, not as an asset call.
                - shared_summary should combine macro and sentiment in one coherent paragraph rather than two separate notes.
                - macro.summary should use only macro facts and explain whether the backdrop is supportive, mixed, or restrictive for crypto risk assets.
                - sentiment.summary should use only sentiment facts and explain whether broad market risk appetite is stretched, recovering, cautious, or mixed.
                - macro.watch_point and sentiment.watch_point must each name the next concrete thing to monitor.
                - If macro facts include concrete values such as DXY, US10Y, USD/KRW, or average-deviation facts, explicitly include at least one of them in macro.summary.
                - If sentiment facts include concrete values such as Fear & Greed value, classification, or greed/fear balance, explicitly include at least one of them in sentiment.summary.
                - Do not write generic prose that could fit any day. Anchor each block to the provided structured facts.
                - Do not restate the same meaning in shared_summary and the domain summaries with only minor wording changes.

                Input JSON:
                %s
                """.formatted(horizonGuidance(input.reportType()), inputJson);

        return new AnalysisLlmPromptComposition(
                SYSTEM_PROMPT,
                userPrompt,
                inputJson,
                OUTPUT_SCHEMA_JSON,
                "{}"
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize shared context prompt input.", exception);
        }
    }

    private String horizonGuidance(AnalysisReportType reportType) {
        if (reportType == null) {
            return "- Use a balanced market-wide backdrop and stay conservative.";
        }
        return switch (reportType) {
            case SHORT_TERM -> """
                    - SHORT_TERM: explain how macro and sentiment may affect near-term risk appetite, short-term momentum continuation, and nearby breakout or hold attempts.
                    - SHORT_TERM: focus on immediate pressure or relief rather than long-cycle meaning.
                    """.strip();
            case MID_TERM -> """
                    - MID_TERM: explain how macro and sentiment may support or constrain multi-day to multi-week structure persistence.
                    - MID_TERM: focus on whether the backdrop helps trend maintenance, consolidation, or structural fatigue.
                    """.strip();
            case LONG_TERM -> """
                    - LONG_TERM: explain how macro and sentiment fit the broader risk-asset regime and long-cycle backdrop.
                    - LONG_TERM: focus on durable environment, regime persistence, and structural tolerance rather than short-lived noise.
                    """.strip();
        };
    }
}

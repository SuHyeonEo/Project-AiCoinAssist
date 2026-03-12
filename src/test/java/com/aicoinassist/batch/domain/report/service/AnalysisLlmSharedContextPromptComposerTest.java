package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmSharedContextPromptComposerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisLlmSharedContextPromptComposer composer =
            new AnalysisLlmSharedContextPromptComposer(objectMapper);

    @Test
    void composeBuildsSharedContextSpecificPromptGuidance() {
        AnalysisLlmSharedContextInputPayload input = new AnalysisLlmSharedContextInputPayload(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "shared-v1",
                "gpt-5.4",
                List.of(
                        "DXY proxy 119.84, US10Y 4.12, USD/KRW 1453.22.",
                        "거시 조합은 위험자산에 다소 부담되는 방향으로 계산됩니다."
                ),
                List.of(
                        "Fear & Greed 72 (Greed).",
                        "LAST_7D 기준 심리 지수는 평균 대비 +18.03%입니다."
                )
        );

        AnalysisLlmPromptComposition composition = composer.compose(input);

        assertThat(composition.systemPrompt()).contains("shared macro and sentiment context");
        assertThat(composition.systemPrompt()).contains("Every sentence must end in expressions such as");
        assertThat(composition.systemPrompt()).contains("Rewrite enum-like English labels into natural Korean");
        assertThat(composition.userPrompt()).contains("SHORT_TERM: explain how macro and sentiment may affect near-term risk appetite");
        assertThat(composition.userPrompt()).contains("focus on immediate pressure or relief rather than long-cycle meaning");
        assertThat(composition.userPrompt()).contains("shared_summary should act as a market-wide backdrop");
        assertThat(composition.userPrompt()).contains("macro.summary should use only macro facts");
        assertThat(composition.userPrompt()).contains("sentiment.summary should use only sentiment facts");
        assertThat(composition.userPrompt()).contains("If macro facts include concrete values such as DXY, US10Y, USD/KRW");
        assertThat(composition.userPrompt()).contains("If sentiment facts include concrete values such as Fear & Greed value");
        assertThat(composition.userPrompt()).contains("Do not write generic prose that could fit any day");
        assertThat(composition.userPrompt()).contains("Do not restate the same meaning in shared_summary and the domain summaries");
        assertThat(composition.inputPayloadJson()).contains("\"macro_facts\"");
        assertThat(composition.inputPayloadJson()).contains("\"sentiment_facts\"");
        assertThat(composition.outputSchemaJson()).contains("\"shared_summary\"");
        assertThat(composition.outputSchemaJson()).contains("\"watch_point\"");
    }
}

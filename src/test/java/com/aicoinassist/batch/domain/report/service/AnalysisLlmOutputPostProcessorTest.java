package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmOutputPostProcessorTest extends AnalysisReportPayloadTestFixtures {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisGptReportInputAssembler gptAssembler =
            new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory());
    private final AnalysisLlmNarrativeInputAssembler llmAssembler = new AnalysisLlmNarrativeInputAssembler();
    private final AnalysisLlmOutputPostProcessor processor =
            new AnalysisLlmOutputPostProcessor(objectMapper, new AnalysisLlmOutputFallbackFactory());

    @Test
    void processNormalizesLongStructuredOutputWithoutFallback() throws Exception {
        AnalysisLlmNarrativeInputPayload input = llmInput();
        AnalysisLlmNarrativeOutputPayload output = new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmExecutiveConclusionOutput(
                        "constructive",
                        List.of(repeat("support", 30), repeat("support", 30), repeat("support", 30), repeat("support", 30)),
                        List.of(repeat("risk", 30), repeat("risk", 30), repeat("risk", 30), repeat("risk", 30)),
                        repeat("summary", 60)
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput(
                                "MARKET",
                                repeat("current-signal", 20),
                                List.of(repeat("fact", 40), repeat("fact", 40), repeat("fact", 40), repeat("fact", 40), repeat("fact", 40)),
                                repeat("interpretation", 40),
                                "mixed",
                                "high",
                                List.of(repeat("caveat", 30), repeat("caveat", 30), repeat("caveat", 30), repeat("caveat", 30))
                        )
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        List.of(repeat("aligned", 30), repeat("aligned", 30), repeat("aligned", 30), repeat("aligned", 30)),
                        List.of(repeat("conflict", 30), repeat("conflict", 30), repeat("conflict", 30), repeat("conflict", 30)),
                        List.of(repeat("driver", 30), repeat("driver", 30), repeat("driver", 30), repeat("driver", 30)),
                        repeat("combined", 50)
                ),
                List.of(
                        new AnalysisLlmScenarioOutput(
                                "bullish",
                                repeat("condition", 30),
                                List.of(repeat("trigger", 30), repeat("trigger", 30), repeat("trigger", 30), repeat("trigger", 30)),
                                List.of(repeat("confirm", 30), repeat("confirm", 30), repeat("confirm", 30), repeat("confirm", 30)),
                                List.of(repeat("invalidate", 30), repeat("invalidate", 30), repeat("invalidate", 30), repeat("invalidate", 30)),
                                repeat("interpretation", 40)
                        )
                ),
                List.of(
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/1",
                                repeat("why", 40),
                                "MACRO"
                        ),
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/2",
                                repeat("why", 40),
                                "MACRO"
                        ),
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/3",
                                repeat("why", 40),
                                "MACRO"
                        ),
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/4",
                                repeat("why", 40),
                                "MACRO"
                        ),
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/5",
                                repeat("why", 40),
                                "MACRO"
                        ),
                        new AnalysisLlmReferenceNewsItem(
                                repeat("headline", 30),
                                "ExampleSource",
                                Instant.parse("2026-03-09T00:30:00Z"),
                                "https://example.com/news/6",
                                repeat("why", 40),
                                "MACRO"
                        )
                )
        );

        var result = processor.process(input, objectMapper.writeValueAsString(output));

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.issues()).isNotEmpty();
        assertThat(result.output().executiveConclusion().summary().length()).isLessThanOrEqualTo(280);
        assertThat(result.output().executiveConclusion().topSupportingFactors()).hasSizeLessThanOrEqualTo(3);
        assertThat(result.output().scenarioMap()).hasSizeLessThanOrEqualTo(3);
        assertThat(result.output().referenceNews()).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void processFallsBackWhenJsonIsInvalid() {
        var result = processor.process(llmInput(), "{invalid json");

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.issues()).isNotEmpty();
        assertThat(result.output().executiveConclusion()).isNotNull();
        assertThat(result.output().domainAnalyses()).isNotEmpty();
        assertThat(result.output().referenceNews()).isEmpty();
    }

    @Test
    void processAcceptsJsonWrappedInMarkdownFence() throws Exception {
        AnalysisLlmNarrativeOutputPayload output = new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmExecutiveConclusionOutput(
                        "mixed",
                        List.of("support"),
                        List.of("risk"),
                        "Short summary"
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput(
                                "MARKET",
                                "Current signal",
                                List.of("Fact"),
                                "Interpretation",
                                "mixed",
                                "medium",
                                List.of("Caveat")
                        )
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        List.of("Aligned"),
                        List.of("Conflicting"),
                        List.of("Driver"),
                        "Combined structure"
                ),
                List.of(
                        new AnalysisLlmScenarioOutput(
                                "neutral",
                                "Condition",
                                List.of("Trigger"),
                                List.of("Confirm"),
                                List.of("Invalidate"),
                                "Interpretation"
                        )
                ),
                List.of()
        );

        var result = processor.process(llmInput(), "```json\n" + objectMapper.writeValueAsString(output) + "\n```");

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.issues()).contains("Removed markdown fences from LLM output.");
        assertThat(result.output().executiveConclusion().summary()).isEqualTo("Short summary");
    }

    private AnalysisLlmNarrativeInputPayload llmInput() {
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        return llmAssembler.assemble(gptAssembler.assemble(entity, shortTermPayload("Prompt summary")));
    }

    private String repeat(String value, int times) {
        return String.join(" ", java.util.Collections.nCopies(times, value));
    }
}

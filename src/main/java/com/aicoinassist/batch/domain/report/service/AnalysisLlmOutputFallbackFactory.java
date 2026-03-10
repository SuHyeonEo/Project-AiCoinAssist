package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptCrossSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainFactBlock;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalysisLlmOutputFallbackFactory {

    public AnalysisLlmNarrativeOutputPayload build(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmNarrativeOutputPayload(
                executiveConclusion(input),
                domainAnalyses(input),
                crossSignalIntegration(input),
                scenarioMap(input),
                List.of()
        );
    }

    private AnalysisLlmExecutiveConclusionOutput executiveConclusion(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmExecutiveConclusionOutput(
                input.executiveSummary() == null || input.executiveSummary().outlook() == null
                        ? "mixed"
                        : input.executiveSummary().outlook().name().toLowerCase(),
                supportingFactors(input),
                riskFactors(input),
                input.executiveSummary() == null
                        ? "Structured fallback summary generated from server-prepared facts."
                        : firstNonBlank(
                                input.executiveSummary().primaryMessage(),
                                input.executiveSummary().headline(),
                                "Structured fallback summary generated from server-prepared facts."
                        )
        );
    }

    private List<AnalysisLlmDomainAnalysisOutput> domainAnalyses(AnalysisLlmNarrativeInputPayload input) {
        return input.domainFactBlocks().stream()
                .map(block -> new AnalysisLlmDomainAnalysisOutput(
                        block.domainType().name(),
                        firstNonBlank(block.summary(), "Structured domain signal available."),
                        limited(block.keyFacts(), 3),
                        firstNonBlank(first(block.keyFacts()), block.summary(), "Interpretation fallback based on structured facts."),
                        inferPressure(block),
                        input.executiveSummary() == null || input.executiveSummary().confidence() == null
                                ? "limited"
                                : input.executiveSummary().confidence().name().toLowerCase(),
                        List.of("Fallback output generated from structured server facts.")
                ))
                .toList();
    }

    private AnalysisLlmCrossSignalIntegrationOutput crossSignalIntegration(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmCrossSignalIntegrationOutput(
                input.crossSignals().stream().map(AnalysisGptCrossSignal::title).limit(3).toList(),
                List.of(),
                input.crossSignals().stream().map(AnalysisGptCrossSignal::summary).limit(3).toList(),
                firstNonBlank(
                        first(input.crossSignals().stream().map(AnalysisGptCrossSignal::summary).toList()),
                        input.executiveSummary() == null ? null : input.executiveSummary().primaryMessage(),
                        "Combined structure is summarized from the available structured cross-signals."
                )
        );
    }

    private List<AnalysisLlmScenarioOutput> scenarioMap(AnalysisLlmNarrativeInputPayload input) {
        return input.scenarios().stream()
                .limit(3)
                .map(this::toScenarioOutput)
                .toList();
    }

    private AnalysisLlmScenarioOutput toScenarioOutput(AnalysisScenario scenario) {
        return new AnalysisLlmScenarioOutput(
                scenario.bias() == null ? "neutral" : scenario.bias().name().toLowerCase(),
                firstNonBlank(first(scenario.triggerConditions()), scenario.title(), "Structured scenario condition."),
                limited(scenario.triggerConditions(), 3),
                List.of(),
                limited(scenario.invalidationSignals(), 3),
                firstNonBlank(scenario.pathSummary(), scenario.title(), "Scenario interpretation fallback.")
        );
    }

    private List<String> supportingFactors(AnalysisLlmNarrativeInputPayload input) {
        List<String> crossSignalTitles = input.crossSignals().stream().map(AnalysisGptCrossSignal::title).limit(2).toList();
        if (!crossSignalTitles.isEmpty()) {
            return crossSignalTitles;
        }
        return input.domainFactBlocks().stream()
                .map(AnalysisLlmDomainFactBlock::headline)
                .limit(2)
                .toList();
    }

    private List<String> riskFactors(AnalysisLlmNarrativeInputPayload input) {
        List<String> riskTitles = input.riskFactors().stream().map(AnalysisRiskFactor::title).limit(3).toList();
        if (!riskTitles.isEmpty()) {
            return riskTitles;
        }
        return List.of("Confidence remains limited to the available structured input.");
    }

    private String inferPressure(AnalysisLlmDomainFactBlock block) {
        String combined = (block.headline() == null ? "" : block.headline() + " ")
                + (block.summary() == null ? "" : block.summary());
        String lower = combined.toLowerCase();
        if (lower.contains("headwind") || lower.contains("pressure") || lower.contains("break risk")
                || lower.contains("crowding") || lower.contains("contraction")) {
            return "bearish";
        }
        if (lower.contains("constructive") || lower.contains("supportive") || lower.contains("expansion")
                || lower.contains("recovery") || lower.contains("continuation")) {
            return "bullish";
        }
        return "mixed";
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private List<String> limited(List<String> values, int size) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(this::hasText).limit(size).toList();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputLengthPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputProcessingResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmReferenceNewsItem;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisLlmOutputPostProcessor {

    private final ObjectMapper objectMapper;
    private final AnalysisLlmOutputFallbackFactory analysisLlmOutputFallbackFactory;

    public AnalysisLlmOutputProcessingResult process(AnalysisLlmNarrativeInputPayload input, String rawOutputJson) {
        AnalysisLlmOutputLengthPolicy policy = AnalysisLlmOutputLengthPolicy.defaultPolicy();
        List<String> issues = new ArrayList<>();
        String sanitizedOutput = sanitize(rawOutputJson, issues);

        AnalysisLlmNarrativeOutputPayload parsedOutput;
        try {
            parsedOutput = objectMapper.readValue(sanitizedOutput, AnalysisLlmNarrativeOutputPayload.class);
        } catch (Exception exception) {
            issues.add("Failed to parse LLM output JSON: " + rootMessage(exception));
            return new AnalysisLlmOutputProcessingResult(
                    analysisLlmOutputFallbackFactory.build(input),
                    true,
                    issues
            );
        }

        AnalysisLlmNarrativeOutputPayload normalized = normalize(parsedOutput, policy, issues);
        List<String> validationIssues = validate(normalized);
        if (!validationIssues.isEmpty()) {
            issues.addAll(validationIssues);
            return new AnalysisLlmOutputProcessingResult(
                    analysisLlmOutputFallbackFactory.build(input),
                    true,
                    issues
            );
        }

        return new AnalysisLlmOutputProcessingResult(normalized, false, issues);
    }

    private String sanitize(String rawOutputJson, List<String> issues) {
        if (rawOutputJson == null) {
            return "";
        }
        String trimmed = rawOutputJson.trim();
        if (trimmed.startsWith("```")) {
            issues.add("Removed markdown fences from LLM output.");
            trimmed = trimmed.replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        return trimmed;
    }

    private AnalysisLlmNarrativeOutputPayload normalize(
            AnalysisLlmNarrativeOutputPayload payload,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmNarrativeOutputPayload(
                normalizeExecutiveConclusion(payload.executiveConclusion(), policy, issues),
                limit(payload.domainAnalyses(), policy.domainAnalysisMaxItems(), "domain_analyses", issues).stream()
                        .map(domain -> normalizeDomainAnalysis(domain, policy, issues))
                        .toList(),
                normalizeCrossSignalIntegration(payload.crossSignalIntegration(), policy, issues),
                limit(payload.scenarioMap(), policy.scenarioMaxItems(), "scenario_map", issues).stream()
                        .map(scenario -> normalizeScenario(scenario, policy, issues))
                        .toList(),
                limit(payload.referenceNews(), policy.referenceNewsMaxItems(), "reference_news", issues).stream()
                        .map(news -> normalizeReferenceNews(news, policy, issues))
                        .toList()
        );
    }

    private AnalysisLlmExecutiveConclusionOutput normalizeExecutiveConclusion(
            AnalysisLlmExecutiveConclusionOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (output == null) {
            return null;
        }
        return new AnalysisLlmExecutiveConclusionOutput(
                truncate(output.overallTone(), 40, "executive_conclusion.overall_tone", issues),
                limitStrings(output.topSupportingFactors(), policy.executiveConclusionFactorMaxItems(),
                        policy.executiveConclusionFactorItemMaxChars(), "executive_conclusion.top_supporting_factors", issues),
                limitStrings(output.topRiskFactors(), policy.executiveConclusionFactorMaxItems(),
                        policy.executiveConclusionFactorItemMaxChars(), "executive_conclusion.top_risk_factors", issues),
                truncate(output.summary(), policy.executiveConclusionSummaryMaxChars(), "executive_conclusion.summary", issues)
        );
    }

    private AnalysisLlmDomainAnalysisOutput normalizeDomainAnalysis(
            AnalysisLlmDomainAnalysisOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmDomainAnalysisOutput(
                truncate(output.domain(), 40, "domain_analyses.domain", issues),
                truncate(output.currentSignal(), policy.domainCurrentSignalMaxChars(), "domain_analyses.current_signal", issues),
                limitStrings(output.keyFacts(), policy.domainKeyFactsMaxItems(), policy.domainKeyFactMaxChars(),
                        "domain_analyses.key_facts", issues),
                truncate(output.interpretation(), policy.domainInterpretationMaxChars(), "domain_analyses.interpretation", issues),
                truncate(output.pressure(), 32, "domain_analyses.pressure", issues),
                truncate(output.confidence(), 32, "domain_analyses.confidence", issues),
                limitStrings(output.caveats(), policy.domainCaveatsMaxItems(), policy.domainCaveatMaxChars(),
                        "domain_analyses.caveats", issues)
        );
    }

    private AnalysisLlmCrossSignalIntegrationOutput normalizeCrossSignalIntegration(
            AnalysisLlmCrossSignalIntegrationOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (output == null) {
            return null;
        }
        return new AnalysisLlmCrossSignalIntegrationOutput(
                limitStrings(output.alignedSignals(), policy.crossSignalListMaxItems(), policy.crossSignalItemMaxChars(),
                        "cross_signal_integration.aligned_signals", issues),
                limitStrings(output.conflictingSignals(), policy.crossSignalListMaxItems(), policy.crossSignalItemMaxChars(),
                        "cross_signal_integration.conflicting_signals", issues),
                limitStrings(output.dominantDrivers(), policy.crossSignalListMaxItems(), policy.crossSignalItemMaxChars(),
                        "cross_signal_integration.dominant_drivers", issues),
                truncate(output.combinedStructure(), policy.crossSignalCombinedStructureMaxChars(),
                        "cross_signal_integration.combined_structure", issues)
        );
    }

    private AnalysisLlmScenarioOutput normalizeScenario(
            AnalysisLlmScenarioOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmScenarioOutput(
                truncate(output.scenarioType(), 32, "scenario_map.scenario_type", issues),
                truncate(output.condition(), policy.scenarioConditionMaxChars(), "scenario_map.condition", issues),
                limitStrings(output.triggers(), policy.scenarioListMaxItems(), policy.scenarioItemMaxChars(),
                        "scenario_map.triggers", issues),
                limitStrings(output.confirmingSignals(), policy.scenarioListMaxItems(), policy.scenarioItemMaxChars(),
                        "scenario_map.confirming_signals", issues),
                limitStrings(output.invalidationSignals(), policy.scenarioListMaxItems(), policy.scenarioItemMaxChars(),
                        "scenario_map.invalidation_signals", issues),
                truncate(output.interpretation(), policy.scenarioInterpretationMaxChars(), "scenario_map.interpretation", issues)
        );
    }

    private AnalysisLlmReferenceNewsItem normalizeReferenceNews(
            AnalysisLlmReferenceNewsItem item,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmReferenceNewsItem(
                truncate(item.title(), policy.referenceNewsTitleMaxChars(), "reference_news.title", issues),
                truncate(item.source(), 64, "reference_news.source", issues),
                item.publishedAt(),
                truncate(item.url(), 512, "reference_news.url", issues),
                truncate(item.whyItMatters(), policy.referenceNewsWhyItMattersMaxChars(), "reference_news.why_it_matters", issues),
                truncate(item.relatedDomain(), 40, "reference_news.related_domain", issues)
        );
    }

    private List<String> validate(AnalysisLlmNarrativeOutputPayload payload) {
        List<String> issues = new ArrayList<>();
        if (payload.executiveConclusion() == null) {
            issues.add("executive_conclusion is required.");
        } else {
            requireText(payload.executiveConclusion().overallTone(), "executive_conclusion.overall_tone", issues);
            requireText(payload.executiveConclusion().summary(), "executive_conclusion.summary", issues);
        }
        if (payload.domainAnalyses() == null || payload.domainAnalyses().isEmpty()) {
            issues.add("domain_analyses must contain at least one item.");
        } else {
            for (AnalysisLlmDomainAnalysisOutput domain : payload.domainAnalyses()) {
                requireText(domain.domain(), "domain_analyses.domain", issues);
                requireText(domain.currentSignal(), "domain_analyses.current_signal", issues);
                requireText(domain.interpretation(), "domain_analyses.interpretation", issues);
                requireText(domain.pressure(), "domain_analyses.pressure", issues);
                requireText(domain.confidence(), "domain_analyses.confidence", issues);
            }
        }
        if (payload.crossSignalIntegration() == null) {
            issues.add("cross_signal_integration is required.");
        } else {
            requireText(payload.crossSignalIntegration().combinedStructure(), "cross_signal_integration.combined_structure", issues);
        }
        if (payload.scenarioMap() == null || payload.scenarioMap().isEmpty()) {
            issues.add("scenario_map must contain at least one scenario.");
        } else {
            for (AnalysisLlmScenarioOutput scenario : payload.scenarioMap()) {
                requireText(scenario.scenarioType(), "scenario_map.scenario_type", issues);
                requireText(scenario.condition(), "scenario_map.condition", issues);
                requireText(scenario.interpretation(), "scenario_map.interpretation", issues);
            }
        }
        if (payload.referenceNews() == null) {
            issues.add("reference_news must be present as an array.");
        }
        return issues;
    }

    private void requireText(String value, String field, List<String> issues) {
        if (value == null || value.isBlank()) {
            issues.add(field + " must not be blank.");
        }
    }

    private <T> List<T> limit(List<T> values, int maxItems, String field, List<String> issues) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        if (values.size() > maxItems) {
            issues.add(field + " was truncated to " + maxItems + " items.");
        }
        return values.stream().limit(maxItems).toList();
    }

    private List<String> limitStrings(List<String> values, int maxItems, int maxChars, String field, List<String> issues) {
        return limit(values, maxItems, field, issues).stream()
                .map(value -> truncate(value, maxChars, field, issues))
                .toList();
    }

    private String truncate(String value, int maxChars, String field, List<String> issues) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        issues.add(field + " was truncated to " + maxChars + " chars.");
        return value.substring(0, Math.max(0, maxChars - 3)).stripTrailing() + "...";
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        return cursor instanceof JsonProcessingException jsonProcessingException
                ? jsonProcessingException.getOriginalMessage()
                : cursor.getMessage();
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmHeroSummaryOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmMarketStructureBoxOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputLengthPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputProcessingResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AnalysisLlmOutputPostProcessor {

    private static final Pattern HANGUL_PATTERN = Pattern.compile(".*[가-힣].*");
    private static final List<String> BLUNT_ENDINGS = List.of(
            "하다", "이다", "보인다", "유효하다", "시사한다"
    );

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

        AnalysisLlmNarrativeOutputPayload normalized = applyServerMarketStructure(
                input,
                normalize(parsedOutput, policy, issues),
                policy,
                issues
        );
        List<String> validationIssues = validate(normalized);
        if (!validationIssues.isEmpty()) {
            issues.addAll(validationIssues);
        }

        return new AnalysisLlmOutputProcessingResult(normalized, false, issues);
    }

    private AnalysisLlmNarrativeOutputPayload applyServerMarketStructure(
            AnalysisLlmNarrativeInputPayload input,
            AnalysisLlmNarrativeOutputPayload payload,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (payload == null || input == null || input.serverMarketStructure() == null) {
            return payload;
        }
        AnalysisLlmMarketStructureBoxOutput original = payload.marketStructureBox();
        AnalysisLlmMarketStructureBoxOutput overridden = new AnalysisLlmMarketStructureBoxOutput(
                truncate(input.serverMarketStructure().rangeLow(), policy.marketStructureValueMaxChars(), "market_structure_box.range_low", issues),
                truncate(input.serverMarketStructure().currentPrice(), policy.marketStructureValueMaxChars(), "market_structure_box.current_price", issues),
                truncate(input.serverMarketStructure().rangeHigh(), policy.marketStructureValueMaxChars(), "market_structure_box.range_high", issues),
                normalizeValueLabelBasis(input.serverMarketStructure().rangePosition(), "market_structure_box.range_position", policy, issues),
                normalizeValueLabelBasis(input.serverMarketStructure().upsideReference(), "market_structure_box.upside_reference", policy, issues),
                normalizeValueLabelBasis(input.serverMarketStructure().downsideReference(), "market_structure_box.downside_reference", policy, issues),
                normalizeValueLabelBasis(input.serverMarketStructure().supportBreakRisk(), "market_structure_box.support_break_risk", policy, issues),
                normalizeValueLabelBasis(input.serverMarketStructure().resistanceBreakRisk(), "market_structure_box.resistance_break_risk", policy, issues),
                original == null ? null : original.interpretation()
        );
        return new AnalysisLlmNarrativeOutputPayload(
                payload.heroSummary(),
                payload.executiveConclusion(),
                payload.domainAnalyses(),
                overridden,
                payload.crossSignalIntegration(),
                payload.scenarioMap()
        );
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
                normalizeHeroSummary(payload.heroSummary(), policy, issues),
                normalizeExecutiveConclusion(payload.executiveConclusion(), policy, issues),
                limit(payload.domainAnalyses(), policy.domainAnalysisMaxItems(), "domain_analyses", issues).stream()
                        .map(domain -> normalizeDomainAnalysis(domain, policy, issues))
                        .toList(),
                normalizeMarketStructureBox(payload.marketStructureBox(), policy, issues),
                normalizeCrossSignalIntegration(payload.crossSignalIntegration(), policy, issues),
                limit(payload.scenarioMap(), policy.scenarioMaxItems(), "scenario_map", issues).stream()
                        .map(scenario -> normalizeScenario(scenario, policy, issues))
                        .toList()
        );
    }

    private AnalysisLlmHeroSummaryOutput normalizeHeroSummary(
            AnalysisLlmHeroSummaryOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (output == null) {
            return null;
        }
        return new AnalysisLlmHeroSummaryOutput(
                truncate(output.marketRegime(), policy.heroSummaryMaxChars(), "hero_summary.market_regime", issues),
                truncate(output.oneLineTake(), policy.heroSummaryMaxChars(), "hero_summary.one_line_take", issues),
                truncate(output.primaryDriver(), policy.heroSummaryMaxChars(), "hero_summary.primary_driver", issues),
                truncate(output.riskDriver(), policy.heroSummaryMaxChars(), "hero_summary.risk_driver", issues)
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
                truncate(output.summary(), policy.executiveConclusionSummaryMaxChars(), "executive_conclusion.summary", issues),
                limitStrings(output.bullishFactors(), policy.executiveConclusionFactorMaxItems(),
                        policy.executiveConclusionFactorItemMaxChars(), "executive_conclusion.bullish_factors", issues),
                limitStrings(output.bearishFactors(), policy.executiveConclusionFactorMaxItems(),
                        policy.executiveConclusionFactorItemMaxChars(), "executive_conclusion.bearish_factors", issues),
                truncate(output.tacticalView(), policy.executiveConclusionTacticalViewMaxChars(), "executive_conclusion.tactical_view", issues)
        );
    }

    private AnalysisLlmDomainAnalysisOutput normalizeDomainAnalysis(
            AnalysisLlmDomainAnalysisOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmDomainAnalysisOutput(
                truncate(output.domain(), 40, "domain_analyses.domain", issues),
                truncate(output.status(), policy.domainStatusMaxChars(), "domain_analyses.status", issues),
                truncate(output.interpretation(), policy.domainInterpretationMaxChars(), "domain_analyses.interpretation", issues),
                truncate(output.watchPoint(), policy.domainWatchPointMaxChars(), "domain_analyses.watch_point", issues)
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
                truncate(output.alignmentSummary(), policy.crossSignalSummaryMaxChars(),
                        "cross_signal_integration.alignment_summary", issues),
                limitStrings(output.dominantDrivers(), policy.crossSignalListMaxItems(), policy.crossSignalItemMaxChars(),
                        "cross_signal_integration.dominant_drivers", issues),
                truncate(output.conflictSummary(), policy.crossSignalSummaryMaxChars(),
                        "cross_signal_integration.conflict_summary", issues),
                truncate(output.positioningTake(), policy.crossSignalPositioningTakeMaxChars(),
                        "cross_signal_integration.positioning_take", issues)
        );
    }

    private AnalysisLlmMarketStructureBoxOutput normalizeMarketStructureBox(
            AnalysisLlmMarketStructureBoxOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (output == null) {
            return null;
        }
        return new AnalysisLlmMarketStructureBoxOutput(
                truncate(output.rangeLow(), policy.marketStructureValueMaxChars(), "market_structure_box.range_low", issues),
                truncate(output.currentPrice(), policy.marketStructureValueMaxChars(), "market_structure_box.current_price", issues),
                truncate(output.rangeHigh(), policy.marketStructureValueMaxChars(), "market_structure_box.range_high", issues),
                normalizeValueLabelBasis(output.rangePosition(), "market_structure_box.range_position", policy, issues),
                normalizeValueLabelBasis(output.upsideReference(), "market_structure_box.upside_reference", policy, issues),
                normalizeValueLabelBasis(output.downsideReference(), "market_structure_box.downside_reference", policy, issues),
                normalizeValueLabelBasis(output.supportBreakRisk(), "market_structure_box.support_break_risk", policy, issues),
                normalizeValueLabelBasis(output.resistanceBreakRisk(), "market_structure_box.resistance_break_risk", policy, issues),
                truncate(output.interpretation(), policy.marketStructureInterpretationMaxChars(), "market_structure_box.interpretation", issues)
        );
    }

    private AnalysisLlmValueLabelBasisOutput normalizeValueLabelBasis(
            AnalysisLlmValueLabelBasisOutput output,
            String field,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        if (output == null) {
            return null;
        }
        return new AnalysisLlmValueLabelBasisOutput(
                truncate(output.value(), policy.marketStructureValueMaxChars(), field + ".value", issues),
                truncate(output.label(), policy.marketStructureLabelMaxChars(), field + ".label", issues),
                truncate(output.basis(), policy.marketStructureBasisMaxChars(), field + ".basis", issues)
        );
    }

    private AnalysisLlmScenarioOutput normalizeScenario(
            AnalysisLlmScenarioOutput output,
            AnalysisLlmOutputLengthPolicy policy,
            List<String> issues
    ) {
        return new AnalysisLlmScenarioOutput(
                truncate(output.scenarioType(), 32, "scenario_map.scenario_type", issues),
                truncate(output.title(), policy.scenarioTitleMaxChars(), "scenario_map.title", issues),
                truncate(output.condition(), policy.scenarioConditionMaxChars(), "scenario_map.condition", issues),
                truncate(output.trigger(), policy.scenarioFieldMaxChars(), "scenario_map.trigger", issues),
                truncate(output.confirmation(), policy.scenarioFieldMaxChars(), "scenario_map.confirmation", issues),
                truncate(output.invalidation(), policy.scenarioFieldMaxChars(), "scenario_map.invalidation", issues),
                truncate(output.interpretation(), policy.scenarioInterpretationMaxChars(), "scenario_map.interpretation", issues)
        );
    }

    private List<String> validate(AnalysisLlmNarrativeOutputPayload payload) {
        List<String> issues = new ArrayList<>();
        if (payload.heroSummary() == null) {
            issues.add("hero_summary is required.");
        } else {
            requireText(payload.heroSummary().marketRegime(), "hero_summary.market_regime", issues);
            requireText(payload.heroSummary().oneLineTake(), "hero_summary.one_line_take", issues);
            requireText(payload.heroSummary().primaryDriver(), "hero_summary.primary_driver", issues);
            requireText(payload.heroSummary().riskDriver(), "hero_summary.risk_driver", issues);
            requirePoliteKorean(payload.heroSummary().marketRegime(), "hero_summary.market_regime", issues);
            requirePoliteKorean(payload.heroSummary().oneLineTake(), "hero_summary.one_line_take", issues);
            requirePoliteKorean(payload.heroSummary().primaryDriver(), "hero_summary.primary_driver", issues);
            requirePoliteKorean(payload.heroSummary().riskDriver(), "hero_summary.risk_driver", issues);
        }
        if (payload.executiveConclusion() == null) {
            issues.add("executive_conclusion is required.");
        } else {
            requireText(payload.executiveConclusion().summary(), "executive_conclusion.summary", issues);
            requireText(payload.executiveConclusion().tacticalView(), "executive_conclusion.tactical_view", issues);
            requirePoliteKorean(payload.executiveConclusion().summary(), "executive_conclusion.summary", issues);
            requirePoliteKorean(payload.executiveConclusion().tacticalView(), "executive_conclusion.tactical_view", issues);
        }
        if (payload.domainAnalyses() == null || payload.domainAnalyses().isEmpty()) {
            issues.add("domain_analyses must contain items.");
        } else if (payload.domainAnalyses().size() != 6) {
            issues.add("domain_analyses must contain exactly 6 items.");
        } else {
            for (AnalysisLlmDomainAnalysisOutput domain : payload.domainAnalyses()) {
                requireText(domain.domain(), "domain_analyses.domain", issues);
                requireText(domain.status(), "domain_analyses.status", issues);
                requireText(domain.interpretation(), "domain_analyses.interpretation", issues);
                requireText(domain.watchPoint(), "domain_analyses.watch_point", issues);
                requirePoliteKorean(domain.interpretation(), "domain_analyses.interpretation", issues);
                requirePoliteKorean(domain.watchPoint(), "domain_analyses.watch_point", issues);
            }
        }
        if (payload.marketStructureBox() == null) {
            issues.add("market_structure_box is required.");
        } else {
            requireText(payload.marketStructureBox().rangeLow(), "market_structure_box.range_low", issues);
            requireText(payload.marketStructureBox().currentPrice(), "market_structure_box.current_price", issues);
            requireText(payload.marketStructureBox().rangeHigh(), "market_structure_box.range_high", issues);
            requireValueLabelBasis(payload.marketStructureBox().rangePosition(), "market_structure_box.range_position", issues);
            requireValueLabelBasis(payload.marketStructureBox().upsideReference(), "market_structure_box.upside_reference", issues);
            requireValueLabelBasis(payload.marketStructureBox().downsideReference(), "market_structure_box.downside_reference", issues);
            requireValueLabelBasis(payload.marketStructureBox().supportBreakRisk(), "market_structure_box.support_break_risk", issues);
            requireValueLabelBasis(payload.marketStructureBox().resistanceBreakRisk(), "market_structure_box.resistance_break_risk", issues);
            requireText(payload.marketStructureBox().interpretation(), "market_structure_box.interpretation", issues);
            requirePoliteKorean(payload.marketStructureBox().interpretation(), "market_structure_box.interpretation", issues);
        }
        if (payload.crossSignalIntegration() == null) {
            issues.add("cross_signal_integration is required.");
        } else {
            requireText(payload.crossSignalIntegration().alignmentSummary(), "cross_signal_integration.alignment_summary", issues);
            requireText(payload.crossSignalIntegration().conflictSummary(), "cross_signal_integration.conflict_summary", issues);
            requireText(payload.crossSignalIntegration().positioningTake(), "cross_signal_integration.positioning_take", issues);
            requirePoliteKorean(payload.crossSignalIntegration().alignmentSummary(), "cross_signal_integration.alignment_summary", issues);
            requirePoliteKorean(payload.crossSignalIntegration().conflictSummary(), "cross_signal_integration.conflict_summary", issues);
            requirePoliteKorean(payload.crossSignalIntegration().positioningTake(), "cross_signal_integration.positioning_take", issues);
        }
        if (payload.scenarioMap() == null || payload.scenarioMap().isEmpty()) {
            issues.add("scenario_map must contain scenarios.");
        } else if (payload.scenarioMap().size() != 3) {
            issues.add("scenario_map must contain exactly 3 scenarios.");
        } else {
            for (AnalysisLlmScenarioOutput scenario : payload.scenarioMap()) {
                requireText(scenario.scenarioType(), "scenario_map.scenario_type", issues);
                requireText(scenario.title(), "scenario_map.title", issues);
                requireText(scenario.condition(), "scenario_map.condition", issues);
                requireText(scenario.trigger(), "scenario_map.trigger", issues);
                requireText(scenario.confirmation(), "scenario_map.confirmation", issues);
                requireText(scenario.invalidation(), "scenario_map.invalidation", issues);
                requireText(scenario.interpretation(), "scenario_map.interpretation", issues);
                requirePoliteKorean(scenario.condition(), "scenario_map.condition", issues);
                requirePoliteKorean(scenario.trigger(), "scenario_map.trigger", issues);
                requirePoliteKorean(scenario.confirmation(), "scenario_map.confirmation", issues);
                requirePoliteKorean(scenario.invalidation(), "scenario_map.invalidation", issues);
                requirePoliteKorean(scenario.interpretation(), "scenario_map.interpretation", issues);
            }
            validateDistinctScenarioNarrative(payload.scenarioMap(), issues);
        }
        return issues;
    }

    private void requireText(String value, String field, List<String> issues) {
        if (value == null || value.isBlank()) {
            issues.add(field + " must not be blank.");
        }
    }

    private void requireValueLabelBasis(AnalysisLlmValueLabelBasisOutput output, String field, List<String> issues) {
        if (output == null) {
            issues.add(field + " is required.");
            return;
        }
        requireText(output.value(), field + ".value", issues);
        requireText(output.label(), field + ".label", issues);
        requireText(output.basis(), field + ".basis", issues);
    }

    private void requirePoliteKorean(String value, String field, List<String> issues) {
        if (value == null || value.isBlank() || !HANGUL_PATTERN.matcher(value).matches()) {
            return;
        }
        String normalized = trimEndingPunctuation(value.strip());
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (BLUNT_ENDINGS.stream().anyMatch(lower::endsWith)) {
            issues.add(field + " must use polite Korean report-style endings.");
            return;
        }
        if (!normalized.endsWith("니다")) {
            issues.add(field + " should end in natural polite Korean report style.");
        }
    }

    private void validateDistinctScenarioNarrative(List<AnalysisLlmScenarioOutput> scenarios, List<String> issues) {
        for (int i = 0; i < scenarios.size(); i++) {
            for (int j = i + 1; j < scenarios.size(); j++) {
                AnalysisLlmScenarioOutput left = scenarios.get(i);
                AnalysisLlmScenarioOutput right = scenarios.get(j);
                if (normalizedComparable(left.trigger()).equals(normalizedComparable(right.trigger()))) {
                    issues.add("scenario_map trigger wording must differ across scenario types.");
                }
                if (normalizedComparable(left.interpretation()).equals(normalizedComparable(right.interpretation()))) {
                    issues.add("scenario_map interpretation wording must differ across scenario types.");
                }
            }
        }
    }

    private String normalizedComparable(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").replaceAll("[.,]", "").strip();
    }

    private String trimEndingPunctuation(String value) {
        return value.replaceAll("[.!?]+$", "");
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
        String normalized = normalizeText(value);
        if (normalized == null || normalized.length() <= maxChars) {
            return normalized;
        }
        issues.add(field + " was truncated to " + maxChars + " chars.");
        return normalized.substring(0, Math.max(0, maxChars - 3)).stripTrailing() + "...";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", " ").strip();
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

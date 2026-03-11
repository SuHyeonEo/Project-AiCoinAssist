package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptCrossSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainFactBlock;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmHeroSummaryOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmMarketStructureBoxOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AnalysisLlmOutputFallbackFactory {

    private static final Pattern RANGE_PATTERN = Pattern.compile("LAST_7D range ([0-9.]+) to ([0-9.]+)");
    private static final Pattern POSITION_PATTERN = Pattern.compile("LAST_7D position ([0-9.]+)%");
    private static final Pattern CURRENT_PRICE_PATTERN = Pattern.compile("현재 가격은 ([0-9.]+)|price at ([0-9.]+)");
    private static final Pattern SUPPORT_PATTERN = Pattern.compile("가까운 지지 구간 ([0-9.]+)~([0-9.]+)");
    private static final Pattern RESISTANCE_PATTERN = Pattern.compile("가까운 저항 구간 ([0-9.]+)~([0-9.]+)");
    private static final Pattern PERCENT_PATTERN = Pattern.compile("([0-9.]+)%");

    public AnalysisLlmNarrativeOutputPayload build(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmNarrativeOutputPayload(
                heroSummary(input),
                executiveConclusion(input),
                domainAnalyses(input),
                marketStructureBox(input),
                crossSignalIntegration(input),
                scenarioMap(input)
        );
    }

    private AnalysisLlmHeroSummaryOutput heroSummary(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmHeroSummaryOutput(
                firstNonBlank(
                        input.executiveSummary() == null || input.executiveSummary().outlook() == null
                                ? null
                                : input.executiveSummary().outlook().name().toLowerCase(),
                        "mixed"
                ),
                firstNonBlank(
                        input.executiveSummary() == null ? null : input.executiveSummary().headline(),
                        input.executiveSummary() == null ? null : input.executiveSummary().primaryMessage(),
                        "구조화된 서버 팩트를 기준으로 현재 흐름을 요약했습니다."
                ),
                firstNonBlank(
                        first(input.crossSignals().stream().map(AnalysisGptCrossSignal::title).toList()),
                        first(input.domainFactBlocks().stream().map(AnalysisLlmDomainFactBlock::headline).toList()),
                        "핵심 동인은 구조화된 입력 범위에서 제한적으로 확인됩니다."
                ),
                firstNonBlank(
                        first(input.riskFactors().stream().map(AnalysisRiskFactor::title).toList()),
                        "주요 리스크는 구조화된 입력 범위에서만 해석 가능합니다."
                )
        );
    }

    private AnalysisLlmExecutiveConclusionOutput executiveConclusion(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmExecutiveConclusionOutput(
                input.executiveSummary() == null
                        ? "구조화된 서버 팩트를 바탕으로 요약한 보수적 해설입니다."
                        : firstNonBlank(
                        input.executiveSummary().primaryMessage(),
                        input.executiveSummary().headline(),
                        "구조화된 서버 팩트를 바탕으로 요약한 보수적 해설입니다."
                ),
                supportingFactors(input),
                riskFactors(input),
                "추가 확인 전까지는 구조화된 팩트를 우선하는 보수적 해석이 적절합니다."
        );
    }

    private List<AnalysisLlmDomainAnalysisOutput> domainAnalyses(AnalysisLlmNarrativeInputPayload input) {
        return input.domainFactBlocks().stream()
                .limit(6)
                .map(block -> new AnalysisLlmDomainAnalysisOutput(
                        block.domainType().name(),
                        inferStatus(block),
                        firstNonBlank(first(block.keyFacts()), block.summary(), "구조화된 서버 팩트를 바탕으로 해석했습니다."),
                        firstNonBlank(first(block.keyFacts()), "다음 구조 변화 여부를 계속 확인할 필요가 있습니다.")
                ))
                .toList();
    }

    private AnalysisLlmCrossSignalIntegrationOutput crossSignalIntegration(AnalysisLlmNarrativeInputPayload input) {
        return new AnalysisLlmCrossSignalIntegrationOutput(
                firstNonBlank(
                        first(input.crossSignals().stream().map(AnalysisGptCrossSignal::summary).toList()),
                        "교차 신호 정렬은 제한적이며 구조화된 입력 범위에서만 해석할 수 있습니다."
                ),
                input.crossSignals().stream().map(AnalysisGptCrossSignal::title).limit(3).toList(),
                firstNonBlank(
                        first(input.crossSignals().stream().map(AnalysisGptCrossSignal::summary).toList()),
                        "서로 충돌하는 신호가 남아 있어 단일 방향 해석은 제한됩니다."
                ),
                firstNonBlank(
                        input.executiveSummary() == null ? null : input.executiveSummary().primaryMessage(),
                        "현재 포지셔닝 해석은 구조화된 크로스 시그널 기준으로 보수적으로 유지합니다."
                )
        );
    }

    private AnalysisLlmMarketStructureBoxOutput marketStructureBox(AnalysisLlmNarrativeInputPayload input) {
        if (input.serverMarketStructure() != null) {
            return new AnalysisLlmMarketStructureBoxOutput(
                    input.serverMarketStructure().rangeLow(),
                    input.serverMarketStructure().currentPrice(),
                    input.serverMarketStructure().rangeHigh(),
                    input.serverMarketStructure().rangePosition(),
                    input.serverMarketStructure().upsideReference(),
                    input.serverMarketStructure().downsideReference(),
                    input.serverMarketStructure().supportBreakRisk(),
                    input.serverMarketStructure().resistanceBreakRisk(),
                    "현재 가격이 상하단 기준 사이 어디에 위치하는지와, 가까운 지지·저항 기준의 유지 여부를 함께 볼 필요가 있습니다."
            );
        }
        String rangeFact = firstContaining(input.primaryFacts(), "LAST_7D range");
        String currentPriceFact = firstNonBlank(
                firstContaining(input.domainFactBlocks().stream().map(AnalysisLlmDomainFactBlock::summary).toList(), "현재 가격은"),
                firstContaining(input.primaryFacts(), "price at")
        );
        String positionFact = firstContaining(input.primaryFacts(), "LAST_7D position");
        String supportFact = firstContaining(input.levelStructureFacts(), "가까운 지지 구간");
        String resistanceFact = firstContaining(input.levelStructureFacts(), "가까운 저항 구간");
        String supportRiskFact = firstContaining(input.domainFactBlocks().stream()
                .filter(block -> block.domainType().name().equals("LEVEL"))
                .flatMap(block -> block.keyFacts().stream())
                .toList(), "지지 이탈 위험");
        String resistanceRiskFact = firstContaining(input.domainFactBlocks().stream()
                .filter(block -> block.domainType().name().equals("LEVEL"))
                .flatMap(block -> block.keyFacts().stream())
                .toList(), "저항 돌파 위험");
        return new AnalysisLlmMarketStructureBoxOutput(
                extractRangeValue(rangeFact, 1, "range low"),
                extractCurrentPrice(currentPriceFact),
                extractRangeValue(rangeFact, 2, "range high"),
                new AnalysisLlmValueLabelBasisOutput(
                        firstNonBlank(extractPercent(positionFact), "n/a"),
                        classifyRangePosition(extractPercentNumber(positionFact)),
                        firstNonBlank(positionFact, "레인지 내 상대 위치 기준")
                ),
                new AnalysisLlmValueLabelBasisOutput(
                        firstNonBlank(extractZoneValue(resistanceFact), "n/a"),
                        "저항 구간",
                        firstNonBlank(resistanceFact, "가까운 상단 기준 가격")
                ),
                new AnalysisLlmValueLabelBasisOutput(
                        firstNonBlank(extractZoneValue(supportFact), "n/a"),
                        "지지 구간",
                        firstNonBlank(supportFact, "가까운 하단 기준 가격")
                ),
                new AnalysisLlmValueLabelBasisOutput(
                        firstNonBlank(extractPercent(supportRiskFact), "n/a"),
                        "지지 이탈 리스크",
                        firstNonBlank(supportRiskFact, "지지 이탈 위험 기준")
                ),
                new AnalysisLlmValueLabelBasisOutput(
                        firstNonBlank(extractPercent(resistanceRiskFact), "n/a"),
                        "저항 돌파 리스크",
                        firstNonBlank(resistanceRiskFact, "저항 돌파 위험 기준")
                ),
                "현재 가격이 상하단 기준 사이 어디에 위치하는지와, 가까운 지지·저항 구간의 재확인 여부를 함께 볼 필요가 있습니다."
        );
    }

    private List<AnalysisLlmScenarioOutput> scenarioMap(AnalysisLlmNarrativeInputPayload input) {
        List<AnalysisLlmScenarioOutput> scenarios = input.scenarios().stream()
                .limit(3)
                .map(this::toScenarioOutput)
                .toList();
        if (!scenarios.isEmpty()) {
            return scenarios;
        }
        return List.of(
                new AnalysisLlmScenarioOutput(
                        "BASE",
                        "기본 시나리오",
                        "구조화된 입력 기준으로 현재 구간 유지 가능성을 우선 확인합니다.",
                        "핵심 레벨과 흐름 유지 여부를 확인합니다.",
                        "도메인 간 정렬이 추가로 맞아떨어지는지 확인합니다.",
                        "구조화된 기준선이 무너지면 해석을 다시 조정해야 합니다.",
                        "현재는 보수적 기준선 확인이 우선입니다."
                )
        );
    }

    private AnalysisLlmScenarioOutput toScenarioOutput(AnalysisScenario scenario) {
        return new AnalysisLlmScenarioOutput(
                normalizeScenarioType(scenario),
                firstNonBlank(scenario.title(), "구조화 시나리오"),
                firstNonBlank(first(scenario.triggerConditions()), scenario.title(), "구조화된 조건을 우선 확인합니다."),
                firstNonBlank(first(scenario.triggerConditions()), "추가 트리거 확인이 필요합니다."),
                firstNonBlank(first(scenario.triggerConditions()), "확인 신호는 구조화 입력 기준으로 제한적입니다."),
                firstNonBlank(first(scenario.invalidationSignals()), "무효화 신호를 함께 확인해야 합니다."),
                firstNonBlank(scenario.pathSummary(), scenario.title(), "구조화된 시나리오 해석입니다.")
        );
    }

    private List<String> supportingFactors(AnalysisLlmNarrativeInputPayload input) {
        List<String> crossSignalTitles = input.crossSignals().stream().map(AnalysisGptCrossSignal::title).limit(3).toList();
        if (!crossSignalTitles.isEmpty()) {
            return crossSignalTitles;
        }
        return input.domainFactBlocks().stream()
                .map(AnalysisLlmDomainFactBlock::headline)
                .limit(3)
                .toList();
    }

    private List<String> riskFactors(AnalysisLlmNarrativeInputPayload input) {
        List<String> riskTitles = input.riskFactors().stream().map(AnalysisRiskFactor::title).limit(3).toList();
        if (!riskTitles.isEmpty()) {
            return riskTitles;
        }
        return List.of("가용한 구조화 입력 범위 안에서만 해석이 가능합니다.");
    }

    private String inferStatus(AnalysisLlmDomainFactBlock block) {
        String combined = (block.headline() == null ? "" : block.headline() + " ")
                + (block.summary() == null ? "" : block.summary());
        String lower = combined.toLowerCase();
        if (lower.contains("headwind") || lower.contains("pressure") || lower.contains("break risk")
                || lower.contains("crowding") || lower.contains("contraction")) {
            return "BEARISH";
        }
        if (lower.contains("constructive") || lower.contains("supportive") || lower.contains("expansion")
                || lower.contains("recovery") || lower.contains("continuation")) {
            return "BULLISH";
        }
        return "MIXED";
    }

    private String normalizeScenarioType(AnalysisScenario scenario) {
        if (scenario.bias() == null) {
            return "BASE";
        }
        return switch (scenario.bias()) {
            case BULLISH -> "BULLISH";
            case BEARISH -> "BEARISH";
            default -> "BASE";
        };
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private String firstContaining(List<String> values, String token) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(this::hasText)
                .filter(value -> value.contains(token))
                .findFirst()
                .orElse(null);
    }

    private String extractRangeValue(String fact, int group, String fallback) {
        if (fact == null) {
            return fallback;
        }
        Matcher matcher = RANGE_PATTERN.matcher(fact);
        return matcher.find() ? matcher.group(group) : fallback;
    }

    private String extractCurrentPrice(String fact) {
        if (fact == null) {
            return "current";
        }
        Matcher matcher = CURRENT_PRICE_PATTERN.matcher(fact);
        if (!matcher.find()) {
            return "current";
        }
        return firstNonBlank(matcher.group(1), matcher.group(2), "current");
    }

    private String extractZoneValue(String fact) {
        if (fact == null) {
            return null;
        }
        Matcher supportMatcher = SUPPORT_PATTERN.matcher(fact);
        if (supportMatcher.find()) {
            return supportMatcher.group(1);
        }
        Matcher resistanceMatcher = RESISTANCE_PATTERN.matcher(fact);
        if (resistanceMatcher.find()) {
            return resistanceMatcher.group(1);
        }
        return null;
    }

    private String extractPercent(String fact) {
        if (fact == null) {
            return null;
        }
        Matcher matcher = PERCENT_PATTERN.matcher(fact);
        return matcher.find() ? matcher.group(1) + "%" : null;
    }

    private Double extractPercentNumber(String fact) {
        if (fact == null) {
            return null;
        }
        Matcher matcher = POSITION_PATTERN.matcher(fact);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
    }

    private String classifyRangePosition(Double value) {
        if (value == null) {
            return "구간 위치는 추가 확인이 필요합니다.";
        }
        if (value < 33.0) {
            return "하단 부근";
        }
        if (value < 45.0) {
            return "하단~중간";
        }
        if (value < 55.0) {
            return "중간 부근";
        }
        if (value < 67.0) {
            return "중간~상단";
        }
        return "상단 부근";
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

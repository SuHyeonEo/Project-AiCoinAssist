package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptCrossSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioGuidance;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainFactBlock;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmServerMarketStructureInput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptSignalBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalysisLlmNarrativeInputAssembler {

    public AnalysisLlmNarrativeInputPayload assemble(AnalysisGptReportInputPayload input) {
        return new AnalysisLlmNarrativeInputPayload(
                input.symbol(),
                input.reportType(),
                input.analysisBasisTime(),
                input.rawReferenceTime(),
                input.sourceDataVersion(),
                input.analysisEngineVersion(),
                executiveSummary(input),
                limit(input.signalHeadlines(), 8),
                limit(input.primaryFacts(), 10),
                marketStructureFacts(input),
                derivativeStructureFacts(input),
                macroStructureFacts(input),
                sentimentStructureFacts(input),
                onchainStructureFacts(input),
                externalStructureFacts(input),
                serverMarketStructure(input),
                levelStructureFacts(input),
                marketStructureBoxFacts(input),
                domainFactBlocks(input),
                limit(input.crossSignals(), 5),
                scenarioGuidance(input),
                limit(input.riskFactors(), 6),
                limit(input.scenarios(), 3),
                limit(input.continuityNotes(), 3)
        );
    }

    private AnalysisLlmExecutiveSummary executiveSummary(AnalysisGptReportInputPayload input) {
        return new AnalysisLlmExecutiveSummary(
                input.summary() == null ? null : input.summary().headline(),
                input.summary() == null ? null : input.summary().outlook(),
                input.summary() == null ? null : input.summary().confidence(),
                input.summary() == null || input.summary().keyMessage() == null ? null : input.summary().keyMessage().primaryMessage()
        );
    }

    private List<AnalysisLlmDomainFactBlock> domainFactBlocks(AnalysisGptReportInputPayload input) {
        List<AnalysisLlmDomainFactBlock> blocks = new ArrayList<>();

        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.MARKET,
                "시장 구조",
                marketStateSummary(input.currentState()),
                gather(
                        movingAverageSummary(input.currentState()),
                        momentumSummary(input.currentState()),
                        input.comparisonContext() == null || input.comparisonContext().factSummary() == null
                                ? null : input.comparisonContext().factSummary().primaryFact(),
                        input.windowContext() == null || input.windowContext().summary() == null
                                ? null : input.windowContext().summary().rangeSummary(),
                        input.windowContext() == null || input.windowContext().summary() == null
                                ? null : input.windowContext().summary().rangePositionSummary()
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.DERIVATIVE,
                "파생 맥락",
                input.derivativeContext() == null ? null : input.derivativeContext().currentStateSummary(),
                gather(
                        input.derivativeContext() == null ? null : input.derivativeContext().windowSummary(),
                        input.derivativeContext() == null ? null : first(input.derivativeContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.MACRO,
                "거시 맥락",
                input.macroContext() == null ? null : input.macroContext().currentStateSummary(),
                gather(
                        input.macroContext() == null ? null : input.macroContext().comparisonSummary(),
                        input.macroContext() == null ? null : input.macroContext().windowSummary(),
                        input.macroContext() == null ? null : first(input.macroContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.SENTIMENT,
                "심리 맥락",
                input.sentimentContext() == null ? null : input.sentimentContext().currentStateSummary(),
                gather(
                        input.sentimentContext() == null ? null : input.sentimentContext().comparisonSummary(),
                        input.sentimentContext() == null ? null : input.sentimentContext().windowSummary(),
                        input.sentimentContext() == null ? null : first(input.sentimentContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.ONCHAIN,
                "온체인 맥락",
                input.onchainContext() == null ? null : input.onchainContext().currentStateSummary(),
                gather(
                        input.onchainContext() == null ? null : input.onchainContext().comparisonSummary(),
                        input.onchainContext() == null ? null : input.onchainContext().windowSummary(),
                        input.onchainContext() == null ? null : first(input.onchainContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.LEVEL,
                "레벨 맥락",
                input.levelContext() == null ? null : first(input.levelContext().zoneInteractionFacts().stream().map(fact -> fact.summary()).toList()),
                gather(
                        nearestZoneSummary("가까운 지지 구간", input.levelContext() == null ? null : input.levelContext().nearestSupportZone()),
                        nearestZoneSummary("가까운 저항 구간", input.levelContext() == null ? null : input.levelContext().nearestResistanceZone()),
                        input.levelContext() == null ? null : "지지 이탈 위험 " + safePercent(input.levelContext().supportBreakRisk()),
                        input.levelContext() == null ? null : "저항 돌파 위험 " + safePercent(input.levelContext().resistanceBreakRisk()),
                        input.levelContext() == null ? null : first(input.levelContext().highlights().stream().map(highlight -> highlight.detail()).toList())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.EXTERNAL,
                "외부 종합 맥락",
                input.externalContextComposite() == null
                        ? null
                        : input.externalContextComposite().state() != null
                                ? input.externalContextComposite().state().summary()
                                : input.externalContextComposite().primarySignalDetail(),
                gather(
                        input.externalContextComposite() == null ? null : first(input.externalContextComposite().highlights().stream().map(highlight -> highlight.summary()).toList()),
                        input.externalContextComposite() == null || input.externalContextComposite().persistence() == null
                                ? null : input.externalContextComposite().persistence().summary(),
                        input.externalContextComposite() == null || input.externalContextComposite().transitions().isEmpty()
                                ? null : input.externalContextComposite().transitions().get(0).summary()
                )
        ));

        return blocks.stream()
                .filter(block -> block.summary() != null || !block.keyFacts().isEmpty())
                .toList();
    }

    private List<String> marketStructureFacts(AnalysisGptReportInputPayload input) {
        return gather(
                movingAverageStackSummary(input.currentState()),
                priceVsMovingAverageSummary(input.currentState()),
                momentumSummary(input.currentState()),
                input.windowContext() == null || input.windowContext().summary() == null
                        ? null : input.windowContext().summary().rangePositionSummary()
        ).stream().limit(4).toList();
    }

    private List<String> levelStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.currentState() == null || input.levelContext() == null) {
            return List.of();
        }
        return gather(
                supportZonePositionFact(input.currentState().currentPrice(), input.levelContext().nearestSupportZone()),
                resistanceZonePositionFact(input.currentState().currentPrice(), input.levelContext().nearestResistanceZone()),
                nearestZoneDistanceFact("가까운 지지 구간", input.levelContext().nearestSupportZone()),
                nearestZoneDistanceFact("가까운 저항 구간", input.levelContext().nearestResistanceZone()),
                levelSignificanceFact("지지", input.levelContext().nearestSupportZone()),
                levelSignificanceFact("저항", input.levelContext().nearestResistanceZone()),
                input.levelContext().zoneInteractionFacts() == null ? null
                        : first(input.levelContext().zoneInteractionFacts().stream().map(fact -> fact.summary()).toList()),
                input.levelContext().supportBreakRisk() == null ? null
                        : "지지 이탈 위험은 " + safePercent(input.levelContext().supportBreakRisk()) + "입니다.",
                input.levelContext().resistanceBreakRisk() == null ? null
                        : "저항 돌파 위험은 " + safePercent(input.levelContext().resistanceBreakRisk()) + "입니다."
        ).stream().limit(6).toList();
    }

    private AnalysisLlmServerMarketStructureInput serverMarketStructure(AnalysisGptReportInputPayload input) {
        AnalysisWindowSummary window = primaryMarketWindow(input);
        if (window == null || input.currentState() == null || input.currentState().currentPrice() == null) {
            return null;
        }
        return new AnalysisLlmServerMarketStructureInput(
                decimal(window.low()),
                decimal(input.currentState().currentPrice()),
                decimal(window.high()),
                new AnalysisLlmValueLabelBasisOutput(
                        safePercent(window.currentPositionInRange()),
                        "레인지 내 위치",
                        window.windowType().name() + " 위치 " + safePercent(window.currentPositionInRange())
                                + ", 고점 대비 거리 " + safePercent(window.distanceFromWindowHigh())
                ),
                marketStructureReference("저항", input.levelContext() == null ? null : input.levelContext().nearestResistanceZone()),
                marketStructureReference("지지", input.levelContext() == null ? null : input.levelContext().nearestSupportZone()),
                marketStructureRisk("지지 이탈 리스크",
                        input.levelContext() == null ? null : input.levelContext().supportBreakRisk(),
                        input.levelContext() == null ? null : input.levelContext().nearestSupportZone()),
                marketStructureRisk("저항 돌파 리스크",
                        input.levelContext() == null ? null : input.levelContext().resistanceBreakRisk(),
                        input.levelContext() == null ? null : input.levelContext().nearestResistanceZone())
        );
    }

    private AnalysisLlmValueLabelBasisOutput marketStructureReference(String side, AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        return new AnalysisLlmValueLabelBasisOutput(
                marketStructureReferenceValue(zone),
                isSingleLevel(zone) ? side + " 기준점" : side + " 구간",
                joinFacts(
                        zone.distanceToZone() == null ? null : "현재가 대비 거리 " + safeAbsolutePercent(zone.distanceToZone()),
                        zone.strongestSourceType() == null ? null : "기준 소스 " + humanizeEnum(zone.strongestSourceType().name()),
                        zone.recentTestCount() == null ? null : "테스트 " + zone.recentTestCount() + "회",
                        zone.recentRejectionCount() == null ? null : "리젝션 " + zone.recentRejectionCount() + "회",
                        zone.recentBreakCount() == null ? null : "이탈 " + zone.recentBreakCount() + "회"
                )
        );
    }

    private AnalysisLlmValueLabelBasisOutput marketStructureRisk(String label, BigDecimal risk, AnalysisPriceZone zone) {
        return new AnalysisLlmValueLabelBasisOutput(
                safePercent(risk),
                label,
                joinFacts(
                        risk == null ? null : label + " " + safePercent(risk),
                        zone == null || zone.distanceToZone() == null ? null : "현재가 대비 거리 " + safeAbsolutePercent(zone.distanceToZone()),
                        zone == null || zone.recentTestCount() == null ? null : "테스트 " + zone.recentTestCount() + "회",
                        zone == null || zone.recentRejectionCount() == null ? null : "리젝션 " + zone.recentRejectionCount() + "회",
                        zone == null || zone.recentBreakCount() == null ? null : "이탈 " + zone.recentBreakCount() + "회"
                )
        );
    }

    private String marketStructureReferenceValue(AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        if (isSingleLevel(zone)) {
            return decimal(firstNonNull(zone.representativePrice(), zone.zoneLow(), zone.zoneHigh()));
        }
        return decimal(zone.zoneLow()) + "~" + decimal(zone.zoneHigh());
    }

    private boolean isSingleLevel(AnalysisPriceZone zone) {
        if (zone == null || zone.zoneLow() == null || zone.zoneHigh() == null) {
            return false;
        }
        return zone.zoneLow().compareTo(zone.zoneHigh()) == 0;
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String joinFacts(String... values) {
        return gather(values).stream().collect(Collectors.joining(", "));
    }

    private List<String> marketStructureBoxFacts(AnalysisGptReportInputPayload input) {
        if (input.currentState() == null || input.levelContext() == null) {
            return List.of();
        }
        return gather(
                "range_position_basis: " + firstNonBlank(
                        input.windowContext() == null || input.windowContext().summary() == null
                                ? null : input.windowContext().summary().rangePositionSummary(),
                        input.windowContext() == null || input.windowContext().summary() == null
                                ? null : input.windowContext().summary().rangeSummary()
                ),
                marketStructureUpsideBasisFact(input),
                marketStructureDownsideBasisFact(input),
                marketStructureSupportRiskBasisFact(input),
                marketStructureResistanceRiskBasisFact(input)
        ).stream().limit(5).toList();
    }

    private List<String> derivativeStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.derivativeFactContext() == null) {
            return List.of();
        }
        AnalysisDerivativeWindowSummary primaryWindow = primaryDerivativeWindow(input.derivativeFactContext(), input.reportType());
        AnalysisDerivativeComparisonFact primaryComparison = primaryDerivativeComparison(input.derivativeFactContext(), input.reportType());
        return gather(
                fundingVsAverageFact(primaryWindow),
                openInterestVsAverageFact(primaryWindow),
                basisVsAverageFact(primaryWindow),
                basisDirectionFact(primaryWindow, input.derivativeContext()),
                openInterestPriceAlignmentFact(primaryWindow, input),
                primaryComparison == null ? null : "기준 비교에서 OI 변화율은 "
                        + safePercent(primaryComparison.openInterestChangeRate()) + "입니다."
        ).stream().limit(6).toList();
    }

    private List<String> externalStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null) {
            return List.of();
        }
        return gather(
                dominantExternalDirectionFact(input),
                compositeRiskFact(input),
                externalWindowDeviationFact(input),
                externalPersistenceFact(input),
                externalReversalRiskFact(input),
                externalSignalCountFact(input)
        ).stream().limit(6).toList();
    }

    private List<String> macroStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.macroFactContext() == null) {
            return List.of();
        }
        AnalysisMacroWindowSummary window = primaryMacroWindow(input.macroFactContext(), input.reportType());
        AnalysisMacroComparisonFact comparison = primaryMacroComparison(input.macroFactContext(), input.reportType());
        return gather(
                input.macroContext() == null ? null : input.macroContext().currentStateSummary(),
                macroDeviationFact(window),
                macroDirectionFact(window),
                comparison == null ? null : "기준 비교에서 DXY 변화율은 " + safePercent(comparison.dxyProxyChangeRate())
                        + ", US10Y 변화율은 " + safePercent(comparison.us10yYieldChangeRate())
                        + ", USD/KRW 변화율은 " + safePercent(comparison.usdKrwChangeRate()) + "입니다."
        ).stream().limit(5).toList();
    }

    private List<String> sentimentStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.sentimentFactContext() == null) {
            return List.of();
        }
        AnalysisSentimentWindowSummary window = primarySentimentWindow(input.sentimentFactContext(), input.reportType());
        AnalysisSentimentComparisonFact comparison = primarySentimentComparison(input.sentimentFactContext(), input.reportType());
        return gather(
                input.sentimentContext() == null ? null : input.sentimentContext().currentStateSummary(),
                sentimentDeviationFact(input.sentimentFactContext(), window),
                sentimentClassificationFact(input.sentimentFactContext(), comparison),
                greedFearBalanceFact(window)
        ).stream().limit(5).toList();
    }

    private List<String> onchainStructureFacts(AnalysisGptReportInputPayload input) {
        if (input.onchainFactContext() == null) {
            return List.of();
        }
        AnalysisOnchainWindowSummary window = primaryOnchainWindow(input.onchainFactContext(), input.reportType());
        AnalysisOnchainComparisonFact comparison = primaryOnchainComparison(input.onchainFactContext(), input.reportType());
        return gather(
                input.onchainContext() == null ? null : input.onchainContext().currentStateSummary(),
                onchainDeviationFact(window),
                onchainParticipationFact(window),
                comparison == null ? null : "기준 비교에서 활성 주소 변화율은 " + safePercent(comparison.activeAddressChangeRate())
                        + ", 트랜잭션 변화율은 " + safePercent(comparison.transactionCountChangeRate())
                        + ", 시가총액 변화율은 " + safePercent(comparison.marketCapChangeRate()) + "입니다."
        ).stream().limit(5).toList();
    }

    private List<AnalysisLlmScenarioGuidance> scenarioGuidance(AnalysisGptReportInputPayload input) {
        return limit(input.scenarios(), 3).stream()
                .map(scenario -> new AnalysisLlmScenarioGuidance(
                        normalizeScenarioType(scenario),
                        scenario.title(),
                        confirmationFacts(scenario, input)
                ))
                .toList();
    }

    private String marketStateSummary(AnalysisCurrentStatePayload currentState) {
        if (currentState == null) {
            return null;
        }
        return "현재 가격은 "
                + decimal(currentState.currentPrice())
                + "이며, 추세는 "
                + trendLabel(currentState.trendLabel().name())
                + ", 변동성은 "
                + volatilityLabel(currentState.volatilityLabel().name())
                + ", 범위 내 위치는 "
                + rangePositionLabel(currentState.rangePositionLabel().name())
                + "입니다.";
    }

    private List<String> gather(String... values) {
        List<String> facts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                facts.add(value);
            }
        }
        return facts;
    }

    private String safePercent(java.math.BigDecimal value) {
        return value == null ? "확인 불가" : value.multiply(new java.math.BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String momentumSummary(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.momentumState() == null) {
            return null;
        }
        List<String> facts = gather(
                currentState.momentumState().rsi14() == null ? null
                        : "RSI14 " + decimal(currentState.momentumState().rsi14()),
                currentState.momentumState().macdHistogram() == null ? null
                        : "MACD 히스토그램 " + decimal(currentState.momentumState().macdHistogram()),
                currentState.momentumState().signalSummary()
        );
        return facts.isEmpty() ? null : String.join(", ", facts);
    }

    private String movingAverageStackSummary(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.movingAveragePositions() == null || currentState.movingAveragePositions().size() < 2) {
            return null;
        }
        List<com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload> sorted = currentState.movingAveragePositions().stream()
                .sorted(Comparator.comparingInt(position -> movingAveragePeriod(position.movingAverageName())))
                .toList();
        boolean bullishStack = true;
        boolean bearishStack = true;
        for (int index = 0; index < sorted.size() - 1; index++) {
            BigDecimal currentLevel = sorted.get(index).level();
            BigDecimal nextLevel = sorted.get(index + 1).level();
            if (currentLevel == null || nextLevel == null) {
                return null;
            }
            if (currentLevel.compareTo(nextLevel) <= 0) {
                bullishStack = false;
            }
            if (currentLevel.compareTo(nextLevel) >= 0) {
                bearishStack = false;
            }
        }
        String sequence = sorted.stream()
                .map(position -> position.movingAverageName() + " " + decimal(position.level()))
                .collect(Collectors.joining(" > "));
        if (bullishStack) {
            return "이동평균 배열은 정배열이며 " + sequence + " 순으로 정렬됩니다.";
        }
        if (bearishStack) {
            return "이동평균 배열은 역배열이며 " + sequence + " 순으로 정렬됩니다.";
        }
        return "이동평균 배열은 혼조이며 " + sequence + " 순으로 배치됩니다.";
    }

    private String movingAverageSummary(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.movingAveragePositions() == null || currentState.movingAveragePositions().isEmpty()) {
            return null;
        }
        String summary = currentState.movingAveragePositions().stream()
                .limit(3)
                .map(position -> position.movingAverageName() + " "
                        + (position.priceAbove() ? "상단" : "하단")
                        + " (" + decimal(position.level()) + ")")
                .collect(Collectors.joining(", "));
        return summary.isBlank() ? null : "이동평균 기준 위치: " + summary;
    }

    private String priceVsMovingAverageSummary(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.movingAveragePositions() == null || currentState.movingAveragePositions().isEmpty()) {
            return null;
        }
        List<String> above = currentState.movingAveragePositions().stream()
                .filter(position -> position.priceAbove())
                .map(position -> position.movingAverageName())
                .toList();
        List<String> below = currentState.movingAveragePositions().stream()
                .filter(position -> !position.priceAbove())
                .map(position -> position.movingAverageName())
                .toList();
        List<String> facts = gather(
                above.isEmpty() ? null : "현재 가격은 " + String.join(", ", above) + " 상단에 있습니다.",
                below.isEmpty() ? null : "현재 가격은 " + String.join(", ", below) + " 하단에 있습니다."
        );
        return facts.isEmpty() ? null : String.join(" ", facts);
    }

    private String nearestZoneSummary(String label, com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        String range = zone.zoneLow() != null && zone.zoneHigh() != null
                ? decimal(zone.zoneLow()) + "~" + decimal(zone.zoneHigh())
                : zone.representativePrice() == null ? null : decimal(zone.representativePrice());
        String distance = zone.distanceToZone() == null ? null : safePercent(zone.distanceToZone());
        List<String> facts = gather(
                range == null ? null : label + " " + range,
                distance == null ? null : "현재가 대비 거리 " + distance,
                zone.recentTestCount() == null ? null : "최근 테스트 " + zone.recentTestCount() + "회",
                zone.recentRejectionCount() == null ? null : "리젝션 " + zone.recentRejectionCount() + "회",
                zone.recentBreakCount() == null ? null : "이탈 " + zone.recentBreakCount() + "회"
        );
        return facts.isEmpty() ? null : String.join(", ", facts);
    }

    private String supportZonePositionFact(BigDecimal currentPrice, AnalysisPriceZone zone) {
        if (currentPrice == null || zone == null || zone.zoneLow() == null || zone.zoneHigh() == null) {
            return null;
        }
        if (currentPrice.compareTo(zone.zoneLow()) < 0) {
            return "현재 가격은 가까운 지지 구간 하단 아래에 있어 지지 이탈 상태로 집계됩니다.";
        }
        if (currentPrice.compareTo(zone.zoneHigh()) > 0) {
            return "현재 가격은 가까운 지지 구간 상단 위에 있어 지지 상단을 유지하고 있습니다.";
        }
        return "현재 가격은 가까운 지지 구간 내부에 위치해 있습니다.";
    }

    private String resistanceZonePositionFact(BigDecimal currentPrice, AnalysisPriceZone zone) {
        if (currentPrice == null || zone == null || zone.zoneLow() == null || zone.zoneHigh() == null) {
            return null;
        }
        if (currentPrice.compareTo(zone.zoneLow()) < 0) {
            return "현재 가격은 가까운 저항 구간 하단 아래에 있어 아직 저항 돌파 전입니다.";
        }
        if (currentPrice.compareTo(zone.zoneHigh()) > 0) {
            return "현재 가격은 가까운 저항 구간 상단 위에 있어 저항 돌파 이후 위치로 집계됩니다.";
        }
        return "현재 가격은 가까운 저항 구간 내부에 진입해 있습니다.";
    }

    private String nearestZoneDistanceFact(String label, AnalysisPriceZone zone) {
        if (zone == null || zone.distanceToZone() == null) {
            return null;
        }
        return label + "까지 현재가 대비 거리는 " + safeAbsolutePercent(zone.distanceToZone()) + "입니다.";
    }

    private String marketStructureUpsideBasisFact(AnalysisGptReportInputPayload input) {
        AnalysisPriceZone zone = input.levelContext() == null ? null : input.levelContext().nearestResistanceZone();
        if (zone == null) {
            return null;
        }
        return "upside_reference_basis: " + String.join(", ", gather(
                nearestReferenceSummary("저항", zone),
                nearestZoneDistanceFact("가까운 저항 구간", zone),
                zone.strongestSourceType() == null ? null : "기준 소스 " + zone.strongestSourceType().name(),
                zoneTestFacts(zone)
        ));
    }

    private String marketStructureDownsideBasisFact(AnalysisGptReportInputPayload input) {
        AnalysisPriceZone zone = input.levelContext() == null ? null : input.levelContext().nearestSupportZone();
        if (zone == null) {
            return null;
        }
        return "downside_reference_basis: " + String.join(", ", gather(
                nearestReferenceSummary("지지", zone),
                nearestZoneDistanceFact("가까운 지지 구간", zone),
                zone.strongestSourceType() == null ? null : "기준 소스 " + zone.strongestSourceType().name(),
                zoneTestFacts(zone)
        ));
    }

    private String marketStructureSupportRiskBasisFact(AnalysisGptReportInputPayload input) {
        if (input.levelContext() == null || input.levelContext().nearestSupportZone() == null) {
            return null;
        }
        AnalysisPriceZone zone = input.levelContext().nearestSupportZone();
        return "support_break_risk_basis: " + String.join(", ", gather(
                "지지 이탈 위험 " + safePercent(input.levelContext().supportBreakRisk()),
                nearestZoneDistanceFact("가까운 지지 구간", zone),
                zoneTestFacts(zone)
        ));
    }

    private String marketStructureResistanceRiskBasisFact(AnalysisGptReportInputPayload input) {
        if (input.levelContext() == null || input.levelContext().nearestResistanceZone() == null) {
            return null;
        }
        AnalysisPriceZone zone = input.levelContext().nearestResistanceZone();
        return "resistance_break_risk_basis: " + String.join(", ", gather(
                "저항 돌파 위험 " + safePercent(input.levelContext().resistanceBreakRisk()),
                nearestZoneDistanceFact("가까운 저항 구간", zone),
                zoneTestFacts(zone)
        ));
    }

    private String zoneTestFacts(AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        return "최근 테스트 " + safeCount(zone.recentTestCount())
                + "회, 리젝션 " + safeCount(zone.recentRejectionCount())
                + "회, 이탈 " + safeCount(zone.recentBreakCount()) + "회";
    }

    private String nearestReferenceSummary(String side, AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        boolean singleLevel = zone.zoneLow() != null
                && zone.zoneHigh() != null
                && zone.zoneLow().compareTo(zone.zoneHigh()) == 0;
        if (singleLevel) {
            BigDecimal point = zone.representativePrice() != null ? zone.representativePrice() : zone.zoneLow();
            return point == null ? null : "가까운 " + side + " 기준점 " + decimal(point);
        }
        if (zone.zoneLow() != null && zone.zoneHigh() != null) {
            return "가까운 " + side + " 구간 " + decimal(zone.zoneLow()) + "~" + decimal(zone.zoneHigh());
        }
        return zone.representativePrice() == null ? null : "가까운 " + side + " 기준점 " + decimal(zone.representativePrice());
    }

    private AnalysisWindowSummary primaryMarketWindow(AnalysisGptReportInputPayload input) {
        if (input == null || input.windowSummaries() == null || input.windowSummaries().isEmpty()) {
            return null;
        }
        List<MarketWindowType> priorities = reportTypeWindowPriority(input.reportType());
        for (MarketWindowType priority : priorities) {
            for (AnalysisWindowSummary summary : input.windowSummaries()) {
                if (summary.windowType() == priority) {
                    return summary;
                }
            }
        }
        return input.windowSummaries().get(0);
    }

    private String levelSignificanceFact(String label, AnalysisPriceZone zone) {
        if (zone == null) {
            return null;
        }
        List<String> counts = gather(
                zone.recentTestCount() == null ? null : "테스트 " + zone.recentTestCount() + "회",
                zone.recentRejectionCount() == null ? null : "리젝션 " + zone.recentRejectionCount() + "회",
                zone.recentBreakCount() == null ? null : "이탈 " + zone.recentBreakCount() + "회"
        );
        if (counts.isEmpty()) {
            return null;
        }
        return "가까운 " + label + " 구간은 최근 " + String.join(", ", counts) + "로 집계됩니다.";
    }

    private List<String> confirmationFacts(AnalysisScenario scenario, AnalysisGptReportInputPayload input) {
        List<String> facts = new ArrayList<>();
        AnalysisScenarioBias bias = scenario.bias() == null ? AnalysisScenarioBias.NEUTRAL : scenario.bias();
        switch (bias) {
            case BULLISH -> {
                addIfPresent(facts, bullishMovingAverageFact(input.currentState()));
                addIfPresent(facts, bullishMomentumFact(input.currentState()));
                addIfPresent(facts, breakoutRiskFact(input));
                addCrossSignalFacts(facts, input.crossSignals(), AnalysisGptSignalBias.SUPPORTIVE);
            }
            case BEARISH -> {
                addIfPresent(facts, bearishMovingAverageFact(input.currentState()));
                addIfPresent(facts, supportRiskFact(input));
                addCrossSignalFacts(facts, input.crossSignals(), AnalysisGptSignalBias.HEADWIND);
                addCrossSignalFacts(facts, input.crossSignals(), AnalysisGptSignalBias.CAUTIONARY);
            }
            default -> {
                addIfPresent(facts, neutralStructureFact(input.currentState()));
                addIfPresent(facts, neutralLevelFact(input));
                addCrossSignalFacts(facts, input.crossSignals(), AnalysisGptSignalBias.MIXED);
            }
        }
        if (facts.isEmpty()) {
            input.primaryFacts().stream().limit(2).forEach(facts::add);
        }
        return facts.stream().distinct().limit(3).toList();
    }

    private String bullishMovingAverageFact(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.movingAveragePositions() == null) {
            return null;
        }
        List<String> above = currentState.movingAveragePositions().stream()
                .filter(position -> position.priceAbove())
                .map(position -> position.movingAverageName())
                .toList();
        return above.size() >= 2 ? "현재 가격이 " + String.join(", ", above) + " 상단에 위치해 있습니다." : null;
    }

    private String bearishMovingAverageFact(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.movingAveragePositions() == null) {
            return null;
        }
        List<String> below = currentState.movingAveragePositions().stream()
                .filter(position -> !position.priceAbove())
                .map(position -> position.movingAverageName())
                .toList();
        return below.size() >= 2 ? "현재 가격이 " + String.join(", ", below) + " 하단에 위치해 있습니다." : null;
    }

    private String bullishMomentumFact(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.momentumState() == null) {
            return null;
        }
        boolean positiveRsi = currentState.momentumState().rsi14() != null
                && currentState.momentumState().rsi14().compareTo(new BigDecimal("55")) >= 0;
        boolean positiveMacd = currentState.momentumState().macdHistogram() != null
                && currentState.momentumState().macdHistogram().compareTo(BigDecimal.ZERO) > 0;
        if (!positiveRsi && !positiveMacd) {
            return null;
        }
        List<String> factors = gather(
                positiveRsi ? "RSI14 " + decimal(currentState.momentumState().rsi14()) : null,
                positiveMacd ? "MACD 히스토그램 " + decimal(currentState.momentumState().macdHistogram()) : null
        );
        return String.join(", ", factors) + "로 모멘텀이 양호하게 유지됩니다.";
    }

    private String breakoutRiskFact(AnalysisGptReportInputPayload input) {
        return input.levelContext() == null || input.levelContext().resistanceBreakRisk() == null
                ? null
                : "저항 돌파 위험은 " + safePercent(input.levelContext().resistanceBreakRisk()) + "로 계산됩니다.";
    }

    private String supportRiskFact(AnalysisGptReportInputPayload input) {
        return input.levelContext() == null || input.levelContext().supportBreakRisk() == null
                ? null
                : "지지 이탈 위험은 " + safePercent(input.levelContext().supportBreakRisk()) + "로 계산됩니다.";
    }

    private String fundingVsAverageFact(AnalysisDerivativeWindowSummary window) {
        if (window == null || window.currentFundingVsAverage() == null) {
            return null;
        }
        BigDecimal value = window.currentFundingVsAverage();
        if (value.compareTo(new BigDecimal("0.50")) >= 0) {
            return window.windowType().name() + " 기준 펀딩은 평균 대비 " + safePercent(value) + " 높아 과열 신호로 계산됩니다.";
        }
        if (value.compareTo(new BigDecimal("-0.15")) <= 0) {
            return window.windowType().name() + " 기준 펀딩은 평균 대비 " + safePercent(value) + " 낮아 약한 포지셔닝으로 계산됩니다.";
        }
        return window.windowType().name() + " 기준 펀딩은 평균 대비 " + safePercent(value) + " 수준으로 중립 범위에 가깝습니다.";
    }

    private String openInterestVsAverageFact(AnalysisDerivativeWindowSummary window) {
        if (window == null || window.currentOpenInterestVsAverage() == null) {
            return null;
        }
        BigDecimal value = window.currentOpenInterestVsAverage();
        if (value.compareTo(new BigDecimal("0.20")) >= 0) {
            return window.windowType().name() + " 기준 OI는 평균 대비 " + safePercent(value) + " 높아 포지션 누적이 커진 상태입니다.";
        }
        if (value.compareTo(new BigDecimal("-0.10")) <= 0) {
            return window.windowType().name() + " 기준 OI는 평균 대비 " + safePercent(value) + " 낮아 포지션 누적이 완화된 상태입니다.";
        }
        return window.windowType().name() + " 기준 OI는 평균 대비 " + safePercent(value) + " 수준으로 큰 쏠림은 제한적입니다.";
    }

    private String basisVsAverageFact(AnalysisDerivativeWindowSummary window) {
        if (window == null || window.currentBasisVsAverage() == null) {
            return null;
        }
        return window.windowType().name() + " 기준 basis는 평균 대비 " + safePercent(window.currentBasisVsAverage()) + "입니다.";
    }

    private String basisDirectionFact(AnalysisDerivativeWindowSummary window, com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload derivativeContext) {
        if (window != null && window.currentBasisVsAverage() != null) {
            if (window.currentBasisVsAverage().compareTo(new BigDecimal("0.10")) >= 0) {
                return "basis는 평균 대비 확대 상태로 집계됩니다.";
            }
            if (window.currentBasisVsAverage().compareTo(new BigDecimal("-0.10")) <= 0) {
                return "basis는 평균 대비 축소 상태로 집계됩니다.";
            }
        }
        return derivativeContext.currentStateSummary();
    }

    private String openInterestPriceAlignmentFact(AnalysisDerivativeWindowSummary window, AnalysisGptReportInputPayload input) {
        if (window == null || window.currentOpenInterestVsAverage() == null
                || input.currentState() == null || input.currentState().trendLabel() == null) {
            return null;
        }
        BigDecimal oi = window.currentOpenInterestVsAverage();
        return switch (input.currentState().trendLabel()) {
            case BULLISH -> oi.compareTo(new BigDecimal("0.10")) >= 0
                    ? "가격 상승 우위와 OI 확대가 함께 나타나 동행 구조로 계산됩니다."
                    : "가격 상승 우위 대비 OI 확대는 제한돼 추격 강도는 아직 제한적입니다.";
            case BEARISH -> oi.compareTo(new BigDecimal("0.10")) >= 0
                    ? "가격 하락 우위와 OI 확대가 함께 나타나 하방 동행 구조로 계산됩니다."
                    : "가격 하락 우위 대비 OI 확대는 제한돼 하방 누적은 아직 과도하지 않습니다.";
            case NEUTRAL -> oi.abs().compareTo(new BigDecimal("0.10")) >= 0
                    ? "가격은 중립이지만 OI 변동이 커 포지셔닝 변화는 존재합니다."
                    : "가격과 OI 모두 뚜렷한 방향 합의는 제한적입니다.";
        };
    }

    private String dominantExternalDirectionFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null) {
            return null;
        }
        String direction = input.externalContextComposite().state() != null
                && input.externalContextComposite().state().dominantDirection() != null
                ? humanizeEnum(input.externalContextComposite().state().dominantDirection().name())
                : input.externalContextComposite().dominantDirection() == null ? null
                : humanizeEnum(input.externalContextComposite().dominantDirection().name());
        if (direction == null) {
            return null;
        }
        return "외부 체계의 우세 방향은 " + direction + "로 집계됩니다.";
    }

    private String compositeRiskFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null || input.externalContextComposite().compositeRiskScore() == null) {
            return null;
        }
        return "외부 composite risk score는 "
                + decimal(input.externalContextComposite().compositeRiskScore().setScale(2, RoundingMode.HALF_UP))
                + "입니다.";
    }

    private String externalWindowDeviationFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null
                || input.externalContextComposite().windowSummaries() == null
                || input.externalContextComposite().windowSummaries().isEmpty()
                || input.externalContextComposite().windowSummaries().get(0).currentCompositeRiskVsAverage() == null) {
            return null;
        }
        var window = input.externalContextComposite().windowSummaries().get(0);
        return window.windowType().name() + " 기준 외부 risk는 평균 대비 "
                + safePercent(window.currentCompositeRiskVsAverage()) + "입니다.";
    }

    private String externalPersistenceFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null || input.externalContextComposite().persistence() == null) {
            return null;
        }
        return input.externalContextComposite().persistence().summary();
    }

    private String externalReversalRiskFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null
                || input.externalContextComposite().state() == null
                || input.externalContextComposite().state().reversalRiskScore() == null) {
            return null;
        }
        return "외부 체계 반전 위험 점수는 "
                + decimal(input.externalContextComposite().state().reversalRiskScore().setScale(2, RoundingMode.HALF_UP))
                + "입니다.";
    }

    private String externalSignalCountFact(AnalysisGptReportInputPayload input) {
        if (input.externalContextComposite() == null) {
            return null;
        }
        return "외부 signal count는 supportive "
                + safeCount(input.externalContextComposite().supportiveSignalCount())
                + ", cautionary "
                + safeCount(input.externalContextComposite().cautionarySignalCount())
                + ", headwind "
                + safeCount(input.externalContextComposite().headwindSignalCount())
                + "입니다.";
    }

    private String macroDeviationFact(AnalysisMacroWindowSummary window) {
        if (window == null) {
            return null;
        }
        return window.windowType().name() + " 기준 DXY "
                + safePercent(window.currentDxyProxyVsAverage())
                + ", US10Y "
                + safePercent(window.currentUs10yYieldVsAverage())
                + ", USD/KRW "
                + safePercent(window.currentUsdKrwVsAverage())
                + "로 평균 대비 편차가 집계됩니다.";
    }

    private String macroDirectionFact(AnalysisMacroWindowSummary window) {
        if (window == null || window.currentDxyProxyVsAverage() == null
                || window.currentUs10yYieldVsAverage() == null || window.currentUsdKrwVsAverage() == null) {
            return null;
        }
        boolean headwind = window.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                || window.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
                || window.currentUsdKrwVsAverage().compareTo(new BigDecimal("0.01")) >= 0;
        boolean supportive = window.currentDxyProxyVsAverage().compareTo(new BigDecimal("-0.01")) <= 0
                && window.currentUs10yYieldVsAverage().compareTo(new BigDecimal("-0.03")) <= 0
                && window.currentUsdKrwVsAverage().compareTo(new BigDecimal("-0.01")) <= 0;
        if (headwind) {
            return "거시 조합은 위험자산에 다소 부담되는 방향으로 계산됩니다.";
        }
        if (supportive) {
            return "거시 조합은 위험자산에 우호적인 방향으로 계산됩니다.";
        }
        return "거시 조합은 뚜렷한 한 방향보다는 혼조에 가깝습니다.";
    }

    private String sentimentDeviationFact(AnalysisSentimentContext context, AnalysisSentimentWindowSummary window) {
        if (context == null) {
            return null;
        }
        if (window == null || window.currentIndexVsAverage() == null) {
            return "Fear & Greed 지수는 " + decimal(context.indexValue()) + "이며 현재 분류는 " + context.classification() + "입니다.";
        }
        return window.windowType().name() + " 기준 심리 지수는 평균 대비 "
                + safePercent(window.currentIndexVsAverage()) + "이며 현재 분류는 " + context.classification() + "입니다.";
    }

    private String sentimentClassificationFact(AnalysisSentimentContext context, AnalysisSentimentComparisonFact comparison) {
        if (context == null) {
            return null;
        }
        if (comparison != null && Boolean.TRUE.equals(comparison.classificationChanged())) {
            return comparison.reference().name() + " 대비 심리 분류가 " + comparison.referenceClassification()
                    + "에서 " + context.classification() + "로 바뀌었습니다.";
        }
        return "현재 심리 분류는 " + context.classification() + "로 유지되고 있습니다.";
    }

    private String greedFearBalanceFact(AnalysisSentimentWindowSummary window) {
        if (window == null || window.greedSampleCount() == null || window.fearSampleCount() == null) {
            return null;
        }
        return window.windowType().name() + " 표본에서 greed "
                + window.greedSampleCount() + "회, fear " + window.fearSampleCount() + "회로 집계됩니다.";
    }

    private String onchainDeviationFact(AnalysisOnchainWindowSummary window) {
        if (window == null) {
            return null;
        }
        return window.windowType().name() + " 기준 활성 주소 "
                + safePercent(window.currentActiveAddressVsAverage())
                + ", 트랜잭션 "
                + safePercent(window.currentTransactionCountVsAverage())
                + ", 시가총액 "
                + safePercent(window.currentMarketCapVsAverage())
                + "로 평균 대비 편차가 집계됩니다.";
    }

    private String onchainParticipationFact(AnalysisOnchainWindowSummary window) {
        if (window == null || window.currentActiveAddressVsAverage() == null || window.currentTransactionCountVsAverage() == null) {
            return null;
        }
        boolean expansion = window.currentActiveAddressVsAverage().compareTo(new BigDecimal("0.10")) >= 0
                && window.currentTransactionCountVsAverage().compareTo(new BigDecimal("0.10")) >= 0;
        boolean contraction = window.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                && window.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0;
        if (expansion) {
            return "온체인 참여는 평균 대비 확장 상태로 계산됩니다.";
        }
        if (contraction) {
            return "온체인 참여는 평균 대비 위축 상태로 계산됩니다.";
        }
        return "온체인 참여는 평균 대비 중립 범위에 가깝습니다.";
    }

    private String neutralStructureFact(AnalysisCurrentStatePayload currentState) {
        if (currentState == null || currentState.rangePositionLabel() == null) {
            return null;
        }
        return "현재 가격의 범위 내 위치는 " + rangePositionLabel(currentState.rangePositionLabel().name()) + "입니다.";
    }

    private String neutralLevelFact(AnalysisGptReportInputPayload input) {
        if (input.currentState() == null || input.levelContext() == null) {
            return null;
        }
        return firstNonBlank(
                resistanceZonePositionFact(input.currentState().currentPrice(), input.levelContext().nearestResistanceZone()),
                supportZonePositionFact(input.currentState().currentPrice(), input.levelContext().nearestSupportZone())
        );
    }

    private void addCrossSignalFacts(List<String> facts, List<AnalysisGptCrossSignal> crossSignals, AnalysisGptSignalBias bias) {
        if (crossSignals == null || crossSignals.isEmpty()) {
            return;
        }
        crossSignals.stream()
                .filter(signal -> signal.bias() == bias)
                .flatMap(signal -> signal.supportingFacts() == null ? java.util.stream.Stream.<String>empty() : signal.supportingFacts().stream().limit(2))
                .filter(value -> value != null && !value.isBlank())
                .limit(2)
                .forEach(facts::add);
    }

    private void addIfPresent(List<String> facts, String value) {
        if (value != null && !value.isBlank()) {
            facts.add(value);
        }
    }

    private String normalizeScenarioType(AnalysisScenario scenario) {
        if (scenario == null || scenario.bias() == null) {
            return "BASE";
        }
        return switch (scenario.bias()) {
            case BULLISH -> "BULLISH";
            case BEARISH -> "BEARISH";
            default -> "BASE";
        };
    }

    private String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String safeAbsolutePercent(BigDecimal value) {
        return value == null ? "확인 불가" : value.abs().multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String trendLabel(String value) {
        return switch (value) {
            case "BULLISH" -> "상승 우위";
            case "BEARISH" -> "하락 우위";
            case "NEUTRAL" -> "중립";
            default -> humanizeEnum(value);
        };
    }

    private String volatilityLabel(String value) {
        return switch (value) {
            case "LOW", "CONTAINED" -> "낮은 편";
            case "MODERATE" -> "보통";
            case "HIGH", "EXPANDING" -> "높은 편";
            default -> humanizeEnum(value);
        };
    }

    private String rangePositionLabel(String value) {
        return switch (value) {
            case "LOWER_RANGE" -> "하단 구간";
            case "MID_RANGE" -> "중간 구간";
            case "UPPER_RANGE" -> "상단 구간";
            case "NEAR_RANGE_LOW" -> "하단 인접 구간";
            case "NEAR_RANGE_HIGH" -> "상단 인접 구간";
            default -> humanizeEnum(value);
        };
    }

    private String humanizeEnum(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private int movingAveragePeriod(String movingAverageName) {
        if (movingAverageName == null) {
            return Integer.MAX_VALUE;
        }
        String digits = movingAverageName.replaceAll("\\D+", "");
        return digits.isBlank() ? Integer.MAX_VALUE : Integer.parseInt(digits);
    }

    private String safeCount(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }

    private AnalysisDerivativeWindowSummary primaryDerivativeWindow(AnalysisDerivativeContext derivativeContext, AnalysisReportType reportType) {
        return selectWindow(derivativeContext == null ? null : derivativeContext.windowSummaries(), reportTypeWindowPriority(reportType));
    }

    private AnalysisDerivativeComparisonFact primaryDerivativeComparison(AnalysisDerivativeContext derivativeContext, AnalysisReportType reportType) {
        return selectComparison(derivativeContext == null ? null : derivativeContext.comparisonFacts(), reportTypeComparisonPriority(reportType));
    }

    private AnalysisMacroWindowSummary primaryMacroWindow(AnalysisMacroContext context, AnalysisReportType reportType) {
        return selectWindow(context == null ? null : context.windowSummaries(), reportTypeWindowPriority(reportType));
    }

    private AnalysisSentimentWindowSummary primarySentimentWindow(AnalysisSentimentContext context, AnalysisReportType reportType) {
        return selectWindow(context == null ? null : context.windowSummaries(), reportTypeWindowPriority(reportType));
    }

    private AnalysisOnchainWindowSummary primaryOnchainWindow(AnalysisOnchainContext context, AnalysisReportType reportType) {
        return selectWindow(context == null ? null : context.windowSummaries(), reportTypeWindowPriority(reportType));
    }

    private AnalysisMacroComparisonFact primaryMacroComparison(AnalysisMacroContext context, AnalysisReportType reportType) {
        return selectComparison(context == null ? null : context.comparisonFacts(), reportTypeComparisonPriority(reportType));
    }

    private AnalysisSentimentComparisonFact primarySentimentComparison(AnalysisSentimentContext context, AnalysisReportType reportType) {
        return selectComparison(context == null ? null : context.comparisonFacts(), reportTypeComparisonPriority(reportType));
    }

    private AnalysisOnchainComparisonFact primaryOnchainComparison(AnalysisOnchainContext context, AnalysisReportType reportType) {
        return selectComparison(context == null ? null : context.comparisonFacts(), reportTypeComparisonPriority(reportType));
    }

    private List<MarketWindowType> reportTypeWindowPriority(AnalysisReportType reportType) {
        if (reportType == null) {
            return List.of(MarketWindowType.LAST_7D, MarketWindowType.LAST_3D, MarketWindowType.LAST_1D);
        }
        return switch (reportType) {
            case SHORT_TERM -> List.of(MarketWindowType.LAST_7D, MarketWindowType.LAST_3D, MarketWindowType.LAST_1D);
            case MID_TERM -> List.of(MarketWindowType.LAST_30D, MarketWindowType.LAST_14D, MarketWindowType.LAST_7D);
            case LONG_TERM -> List.of(MarketWindowType.LAST_180D, MarketWindowType.LAST_90D, MarketWindowType.LAST_30D);
        };
    }

    private List<com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference> reportTypeComparisonPriority(AnalysisReportType reportType) {
        if (reportType == null) {
            return List.of(com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.PREV_BATCH,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D1,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D3);
        }
        return switch (reportType) {
            case SHORT_TERM -> List.of(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.PREV_BATCH,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D1,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D3
            );
            case MID_TERM -> List.of(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D7,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D14,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D30
            );
            case LONG_TERM -> List.of(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D180,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D90,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D30
            );
        };
    }

    private <T extends Record> T selectWindow(List<T> windows, List<MarketWindowType> priorities) {
        if (windows == null || windows.isEmpty()) {
            return null;
        }
        for (MarketWindowType priority : priorities) {
            for (T window : windows) {
                MarketWindowType windowType = extractWindowType(window);
                if (windowType == priority) {
                    return window;
                }
            }
        }
        return windows.get(windows.size() - 1);
    }

    private <T extends Record> T selectComparison(List<T> facts, List<com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference> priorities) {
        if (facts == null || facts.isEmpty()) {
            return null;
        }
        for (com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference priority : priorities) {
            for (T fact : facts) {
                com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference reference = extractReference(fact);
                if (reference == priority) {
                    return fact;
                }
            }
        }
        return facts.get(0);
    }

    private MarketWindowType extractWindowType(Record window) {
        return switch (window) {
            case AnalysisDerivativeWindowSummary value -> value.windowType();
            case AnalysisMacroWindowSummary value -> value.windowType();
            case AnalysisSentimentWindowSummary value -> value.windowType();
            case AnalysisOnchainWindowSummary value -> value.windowType();
            default -> null;
        };
    }

    private com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference extractReference(Record fact) {
        return switch (fact) {
            case AnalysisDerivativeComparisonFact value -> value.reference();
            case AnalysisMacroComparisonFact value -> value.reference();
            case AnalysisSentimentComparisonFact value -> value.reference();
            case AnalysisOnchainComparisonFact value -> value.reference();
            default -> null;
        };
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private <T> List<T> limit(List<T> values, int limit) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().limit(limit).toList();
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainFactBlock;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                domainFactBlocks(input),
                limit(input.crossSignals(), 5),
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

    private String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
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

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private <T> List<T> limit(List<T> values, int limit) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().limit(limit).toList();
    }
}

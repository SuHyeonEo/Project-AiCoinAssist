package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainFactBlock;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                "Market state",
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
                "Derivative context",
                input.derivativeContext() == null ? null : input.derivativeContext().currentStateSummary(),
                gather(
                        input.derivativeContext() == null ? null : input.derivativeContext().windowSummary(),
                        input.derivativeContext() == null ? null : first(input.derivativeContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.MACRO,
                "Macro context",
                input.macroContext() == null ? null : input.macroContext().currentStateSummary(),
                gather(
                        input.macroContext() == null ? null : input.macroContext().comparisonSummary(),
                        input.macroContext() == null ? null : input.macroContext().windowSummary(),
                        input.macroContext() == null ? null : first(input.macroContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.SENTIMENT,
                "Sentiment context",
                input.sentimentContext() == null ? null : input.sentimentContext().currentStateSummary(),
                gather(
                        input.sentimentContext() == null ? null : input.sentimentContext().comparisonSummary(),
                        input.sentimentContext() == null ? null : input.sentimentContext().windowSummary(),
                        input.sentimentContext() == null ? null : first(input.sentimentContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.ONCHAIN,
                "On-chain context",
                input.onchainContext() == null ? null : input.onchainContext().currentStateSummary(),
                gather(
                        input.onchainContext() == null ? null : input.onchainContext().comparisonSummary(),
                        input.onchainContext() == null ? null : input.onchainContext().windowSummary(),
                        input.onchainContext() == null ? null : first(input.onchainContext().highlightDetails())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.LEVEL,
                "Level context",
                input.levelContext() == null ? null : first(input.levelContext().zoneInteractionFacts().stream().map(fact -> fact.summary()).toList()),
                gather(
                        input.levelContext() == null ? null : "Support break risk " + safePercent(input.levelContext().supportBreakRisk()),
                        input.levelContext() == null ? null : "Resistance break risk " + safePercent(input.levelContext().resistanceBreakRisk()),
                        input.levelContext() == null ? null : first(input.levelContext().highlights().stream().map(highlight -> highlight.detail()).toList())
                )
        ));
        blocks.add(new AnalysisLlmDomainFactBlock(
                AnalysisLlmDomainType.EXTERNAL,
                "External composite",
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
        return "Current price "
                + currentState.currentPrice().stripTrailingZeros().toPlainString()
                + ", trend "
                + currentState.trendLabel().name()
                + ", volatility "
                + currentState.volatilityLabel().name()
                + ", range position "
                + currentState.rangePositionLabel().name();
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
        return value == null ? "unavailable" : value.multiply(new java.math.BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
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

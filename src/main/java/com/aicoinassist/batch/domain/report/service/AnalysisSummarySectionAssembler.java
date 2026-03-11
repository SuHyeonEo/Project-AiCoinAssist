package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryKeyMessagePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;

import java.util.ArrayList;
import java.util.List;

class AnalysisSummarySectionAssembler {

    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisComparisonWindowSupport comparisonWindowSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;
    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisSummarySectionAssembler(
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisComparisonWindowSupport comparisonWindowSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport,
            AnalysisMacroContextSupport macroContextSupport,
            AnalysisSentimentContextSupport sentimentContextSupport,
            AnalysisOnchainContextSupport onchainContextSupport,
            AnalysisReportFormattingSupport formattingSupport
    ) {
        this.indicatorStateSupport = indicatorStateSupport;
        this.comparisonWindowSupport = comparisonWindowSupport;
        this.derivativeContextSupport = derivativeContextSupport;
        this.macroContextSupport = macroContextSupport;
        this.sentimentContextSupport = sentimentContextSupport;
        this.onchainContextSupport = onchainContextSupport;
        this.formattingSupport = formattingSupport;
    }

    AnalysisSummaryPayload buildSummary(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            List<AnalysisContinuityNote> continuityNotes,
            AnalysisExternalContextCompositePayload externalContextComposite,
            AnalysisLevelContextPayload levelContext
    ) {
        String headline = reportType.name() + " view";
        AnalysisContextHeadlinePayload comparisonHeadline = comparisonWindowSupport.comparisonContextHeadline(
                reportType,
                comparisonFacts,
                comparisonHighlights
        );
        AnalysisContextHeadlinePayload windowHeadline = comparisonWindowSupport.windowContextHeadline(reportType, windowSummaries);
        AnalysisContextHeadlinePayload derivativeHeadline = derivativeContextSupport.derivativeContextHeadline(
                reportType,
                derivativeContext
        );
        AnalysisContextHeadlinePayload macroHeadline = macroContextSupport.macroContextHeadline(
                reportType,
                macroContext
        );
        AnalysisContextHeadlinePayload sentimentHeadline = sentimentContextSupport.sentimentContextHeadline(
                reportType,
                sentimentContext
        );
        AnalysisContextHeadlinePayload onchainHeadline = onchainContextSupport.onchainContextHeadline(
                reportType,
                onchainContext
        );
        AnalysisContextHeadlinePayload externalHeadline = externalContextHeadline(externalContextComposite);
        List<AnalysisContextHeadlinePayload> signalHeadlines = java.util.stream.Stream.of(
                        comparisonHeadline,
                        windowHeadline,
                        derivativeHeadline,
                        macroHeadline,
                        sentimentHeadline,
                        onchainHeadline,
                        externalHeadline
                )
                .filter(java.util.Objects::nonNull)
                .toList();

        AnalysisSummaryKeyMessagePayload keyMessage = new AnalysisSummaryKeyMessagePayload(
                snapshot.getSymbol()
                        + " is in a "
                        + formattingSupport.enumLabel(trendBias)
                        + " structure with price at "
                        + snapshot.getCurrentPrice().stripTrailingZeros().toPlainString()
                        + ", RSI14 at "
                        + snapshot.getRsi14().stripTrailingZeros().toPlainString()
                        + ", and MACD histogram at "
                        + snapshot.getMacdHistogram().stripTrailingZeros().toPlainString()
                        + ". "
                        + externalContextSentence(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                summarySignalDetails(signalHeadlines, levelContext),
                continuityNotes.isEmpty() ? null : continuityNotes.get(0).summary()
        );

        AnalysisOutlookType outlook = switch (trendBias) {
            case BULLISH -> AnalysisOutlookType.CONSTRUCTIVE;
            case BEARISH -> AnalysisOutlookType.DEFENSIVE;
            case NEUTRAL -> AnalysisOutlookType.NEUTRAL;
        };

        return new AnalysisSummaryPayload(
                headline,
                outlook,
                indicatorStateSupport.confidenceLabel(snapshot, comparisonFacts, derivativeContext),
                keyMessage,
                signalHeadlines
        );
    }

    private List<String> summarySignalDetails(
            List<AnalysisContextHeadlinePayload> signalHeadlines,
            AnalysisLevelContextPayload levelContext
    ) {
        List<String> details = new ArrayList<>(signalHeadlines.stream()
                                                              .map(AnalysisContextHeadlinePayload::detail)
                                                              .toList());
        if (levelContext.nearestSupportZone() != null) {
            details.add("Nearest support %s with %d clustered levels."
                                .formatted(
                                        formattingSupport.zoneLabel(
                                                levelContext.nearestSupportZone().zoneLow(),
                                                levelContext.nearestSupportZone().zoneHigh()
                                        ),
                                        levelContext.nearestSupportZone().levelCount()
                                ));
        }
        if (levelContext.nearestResistanceZone() != null) {
            details.add("Nearest resistance %s with %d clustered levels."
                                .formatted(
                                        formattingSupport.zoneLabel(
                                                levelContext.nearestResistanceZone().zoneLow(),
                                                levelContext.nearestResistanceZone().zoneHigh()
                                        ),
                                        levelContext.nearestResistanceZone().levelCount()
                                ));
        }
        levelContext.zoneInteractionFacts().stream()
                    .map(AnalysisZoneInteractionFact::summary)
                    .forEach(details::add);
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .forEach(details::add);
        if (levelContext != null && levelContext.comparisonFacts() != null) {
            levelContext.comparisonFacts().stream()
                        .limit(2)
                        .map(fact -> fact.reference().name() + " level context changes incorporated")
                        .forEach(details::add);
        }
        return details;
    }

    private String externalContextSentence(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<String> clauses = new ArrayList<>();

        if (macroContext != null) {
            var macroWindowSummary = macroContextSupport.primaryWindowSummary(reportType, macroContext);
            if (macroWindowSummary != null) {
                clauses.add("Macro context keeps DXY "
                        + formattingSupport.signedRatio(macroWindowSummary.currentDxyProxyVsAverage())
                        + " versus average");
            }
        }

        if (sentimentContext != null) {
            var sentimentWindowSummary = sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
            if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null) {
                clauses.add("sentiment stays "
                        + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage())
                        + " versus average");
            }
        }

        if (onchainContext != null) {
            var onchainWindowSummary = onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
            if (onchainWindowSummary != null && onchainWindowSummary.currentActiveAddressVsAverage() != null) {
                clauses.add("on-chain activity runs "
                        + formattingSupport.signedRatio(onchainWindowSummary.currentActiveAddressVsAverage())
                        + " versus average");
            }
        }

        if (externalContextComposite != null && externalContextComposite.primarySignalTitle() != null) {
            if (externalContextComposite.transitions() != null && !externalContextComposite.transitions().isEmpty()) {
                clauses.add(externalContextComposite.transitions().get(0).summary());
            } else if (externalContextComposite.highlights() != null && !externalContextComposite.highlights().isEmpty()) {
                clauses.add(externalContextComposite.highlights().get(0).summary());
            } else {
                clauses.add("primary external regime is "
                        + externalContextComposite.primarySignalTitle().toLowerCase()
                        + " with composite risk score "
                        + externalContextComposite.compositeRiskScore().setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
            }
        }

        if (externalContextComposite != null
                && externalContextComposite.windowSummaries() != null
                && !externalContextComposite.windowSummaries().isEmpty()) {
            var primaryWindowSummary = externalContextComposite.windowSummaries().get(externalContextComposite.windowSummaries().size() - 1);
            if (primaryWindowSummary.currentCompositeRiskVsAverage() != null) {
                clauses.add(primaryWindowSummary.windowType().name()
                        + " external risk stays "
                        + formattingSupport.signedRatio(primaryWindowSummary.currentCompositeRiskVsAverage())
                        + " versus average");
            }
        }
        if (externalContextComposite != null && externalContextComposite.persistence() != null) {
            clauses.add(externalContextComposite.persistence().summary());
        }
        if (externalContextComposite != null && externalContextComposite.state() != null) {
            clauses.add("external reversal risk is "
                    + formattingSupport.signedRatio(externalContextComposite.state().reversalRiskScore())
                    .replace("+", "")
                    + " of full scale");
        }

        return clauses.isEmpty() ? "External context stays mixed." : String.join(", ", clauses) + ".";
    }

    private AnalysisContextHeadlinePayload externalContextHeadline(
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        if (externalContextComposite == null) {
            return null;
        }
        if (externalContextComposite.transitions() != null && !externalContextComposite.transitions().isEmpty()) {
            var transition = externalContextComposite.transitions().get(0);
            return new AnalysisContextHeadlinePayload(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                    transition.transitionType().name(),
                    transition.summary(),
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance.HIGH
            );
        }
        if (externalContextComposite.highlights() != null && !externalContextComposite.highlights().isEmpty()) {
            var highlight = externalContextComposite.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                    highlight.title(),
                    highlight.summary(),
                    highlight.importance()
            );
        }
        if (externalContextComposite.primarySignalTitle() == null) {
            return null;
        }
        return new AnalysisContextHeadlinePayload(
                com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                externalContextComposite.primarySignalTitle(),
                externalContextComposite.primarySignalDetail(),
                com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance.MEDIUM
        );
    }
}

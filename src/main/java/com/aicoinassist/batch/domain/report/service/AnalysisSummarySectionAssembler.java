package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
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
    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisSummarySectionAssembler(
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisComparisonWindowSupport comparisonWindowSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport,
            AnalysisReportFormattingSupport formattingSupport
    ) {
        this.indicatorStateSupport = indicatorStateSupport;
        this.comparisonWindowSupport = comparisonWindowSupport;
        this.derivativeContextSupport = derivativeContextSupport;
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
            List<AnalysisContinuityNote> continuityNotes,
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
        List<AnalysisContextHeadlinePayload> signalHeadlines = java.util.stream.Stream.of(
                        comparisonHeadline,
                        windowHeadline,
                        derivativeHeadline
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
                        + ".",
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
            details.add("Nearest support zone %s to %s with %d clustered levels."
                                .formatted(
                                        levelContext.nearestSupportZone().zoneLow().stripTrailingZeros().toPlainString(),
                                        levelContext.nearestSupportZone().zoneHigh().stripTrailingZeros().toPlainString(),
                                        levelContext.nearestSupportZone().levelCount()
                                ));
        }
        if (levelContext.nearestResistanceZone() != null) {
            details.add("Nearest resistance zone %s to %s with %d clustered levels."
                                .formatted(
                                        levelContext.nearestResistanceZone().zoneLow().stripTrailingZeros().toPlainString(),
                                        levelContext.nearestResistanceZone().zoneHigh().stripTrailingZeros().toPlainString(),
                                        levelContext.nearestResistanceZone().levelCount()
                                ));
        }
        levelContext.zoneInteractionFacts().stream()
                    .map(AnalysisZoneInteractionFact::summary)
                    .forEach(details::add);
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .forEach(details::add);
        return details;
    }
}

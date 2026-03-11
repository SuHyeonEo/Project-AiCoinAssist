package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisComparisonWindowSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisComparisonWindowSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    List<AnalysisComparisonHighlight> comparisonHighlights(
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts
    ) {
        Map<AnalysisComparisonReference, AnalysisComparisonFact> factByReference = comparisonFacts.stream()
                                                                                                  .collect(Collectors.toMap(
                                                                                                          AnalysisComparisonFact::reference,
                                                                                                          fact -> fact,
                                                                                                          (left, right) -> left
                                                                                                  ));

        return highlightPriority(reportType).stream()
                                            .map(factByReference::get)
                                            .filter(java.util.Objects::nonNull)
                                            .map(this::toHighlight)
                                            .toList();
    }

    List<AnalysisWindowHighlight> windowHighlights(
            AnalysisReportType reportType,
            List<AnalysisWindowSummary> windowSummaries
    ) {
        Map<MarketWindowType, AnalysisWindowSummary> summaryByType = windowSummaries.stream()
                                                                                    .collect(Collectors.toMap(
                                                                                            AnalysisWindowSummary::windowType,
                                                                                            summary -> summary,
                                                                                            (left, right) -> left
                                                                                    ));

        return (switch (reportType) {
            case SHORT_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_1D),
                    summaryByType.get(MarketWindowType.LAST_7D)
            );
            case MID_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_7D),
                    summaryByType.get(MarketWindowType.LAST_30D)
            );
            case LONG_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_180D),
                    summaryByType.get(MarketWindowType.LAST_52W)
            );
        }).stream()
         .filter(java.util.Objects::nonNull)
         .map(this::toWindowHighlight)
         .toList();
    }

    AnalysisContextHeadlinePayload comparisonContextHeadline(
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights
    ) {
        if (!comparisonHighlights.isEmpty()) {
            AnalysisComparisonHighlight highlight = comparisonHighlights.get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.COMPARISON,
                    highlight.reference().name() + " comparison",
                    highlight.detail(),
                    reportType == AnalysisReportType.SHORT_TERM
                            ? AnalysisContextHeadlineImportance.HIGH
                            : AnalysisContextHeadlineImportance.MEDIUM
            );
        }
        if (!comparisonFacts.isEmpty()) {
            AnalysisComparisonFact fact = comparisonFacts.get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.COMPARISON,
                    fact.reference().name() + " comparison",
                    comparisonFactSummary(fact),
                    AnalysisContextHeadlineImportance.MEDIUM
            );
        }
        return null;
    }

    AnalysisContextHeadlinePayload windowContextHeadline(
            AnalysisReportType reportType,
            List<AnalysisWindowSummary> windowSummaries
    ) {
        AnalysisWindowSummary primaryWindow = primaryWindow(windowSummaries);
        if (primaryWindow == null) {
            return null;
        }
        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.WINDOW,
                primaryWindow.windowType().name() + " position",
                primaryWindow.windowType().name()
                        + " keeps price at "
                        + formattingSupport.percentage(primaryWindow.currentPositionInRange())
                        + " of the range with volume "
                        + formattingSupport.signedRatio(primaryWindow.currentVolumeVsAverage())
                        + " versus average.",
                reportType == AnalysisReportType.LONG_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    AnalysisWindowSummary primaryWindow(List<AnalysisWindowSummary> windowSummaries) {
        if (windowSummaries == null || windowSummaries.isEmpty()) {
            return null;
        }
        return windowSummaries.get(windowSummaries.size() - 1);
    }

    String comparisonFactSummary(AnalysisComparisonFact fact) {
        return fact.reference().name()
                + " price "
                + formattingSupport.signed(fact.priceChangeRate())
                + "%, RSI delta "
                + formattingSupport.signed(fact.rsiDelta())
                + ", MACD hist delta "
                + formattingSupport.signed(fact.macdHistogramDelta());
    }

    private List<AnalysisComparisonReference> highlightPriority(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> List.of(
                    AnalysisComparisonReference.PREV_BATCH,
                    AnalysisComparisonReference.D1,
                    AnalysisComparisonReference.D3
            );
            case MID_TERM -> List.of(
                    AnalysisComparisonReference.D7,
                    AnalysisComparisonReference.D14,
                    AnalysisComparisonReference.D30
            );
            case LONG_TERM -> List.of(
                    AnalysisComparisonReference.Y52_HIGH,
                    AnalysisComparisonReference.Y52_LOW,
                    AnalysisComparisonReference.D180
            );
        };
    }

    private AnalysisComparisonHighlight toHighlight(AnalysisComparisonFact fact) {
        return switch (fact.reference()) {
            case PREV_BATCH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Since the previous batch, price changed " + formattingSupport.signed(fact.priceChangeRate()) + "% and RSI14 moved " + formattingSupport.signed(fact.rsiDelta()) + ".",
                    "PREV_BATCH confirms the latest impulse with MACD histogram delta " + formattingSupport.signed(fact.macdHistogramDelta()) + "."
            );
            case D1, D3, D7, D14, D30, D90, D180 -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    fact.reference().name() + " shows price " + formattingSupport.signed(fact.priceChangeRate()) + "% versus the reference point.",
                    fact.reference().name() + " keeps RSI delta " + formattingSupport.signed(fact.rsiDelta()) + " and MACD hist delta " + formattingSupport.signed(fact.macdHistogramDelta()) + "."
            );
            case PREV_SHORT_REPORT, PREV_MID_REPORT, PREV_LONG_REPORT -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Versus " + fact.reference().name() + ", price changed " + formattingSupport.signed(fact.priceChangeRate()) + "%.",
                    fact.reference().name() + " comparison shows RSI delta " + formattingSupport.signed(fact.rsiDelta()) + " and ATR change " + formattingSupport.signed(fact.atrChangeRate()) + "%."
            );
            case Y52_HIGH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Price is " + formattingSupport.distanceFromExtremum(fact.priceChangeRate(), "below") + " the 52-week high.",
                    "Y52_HIGH keeps long-term upside distance at " + formattingSupport.signed(fact.priceChangeRate()) + "% from the cycle peak."
            );
            case Y52_LOW -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Price is " + formattingSupport.distanceFromExtremum(fact.priceChangeRate(), "above") + " the 52-week low.",
                    "Y52_LOW shows the market remains " + formattingSupport.signed(fact.priceChangeRate()) + "% above the cycle floor."
            );
        };
    }

    private AnalysisWindowHighlight toWindowHighlight(AnalysisWindowSummary summary) {
        return new AnalysisWindowHighlight(
                summary.windowType(),
                summary.windowType().name() + " keeps price at " + formattingSupport.percentage(summary.currentPositionInRange()) + " of the range.",
                summary.windowType().name()
                        + " volume vs average "
                        + formattingSupport.signedRatio(summary.currentVolumeVsAverage())
                        + ", ATR vs average "
                        + formattingSupport.signedRatio(summary.currentAtrVsAverage())
                        + ", distance from range high "
                        + formattingSupport.percentage(summary.distanceFromWindowHigh())
                        + "."
        );
    }
}

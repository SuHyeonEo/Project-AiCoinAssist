package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisMacroHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisMacroContextSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisMacroContextSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    AnalysisMacroContext enrichMacroContext(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        if (macroContext == null) {
            return null;
        }

        return new AnalysisMacroContext(
                macroContext.snapshotTime(),
                macroContext.sourceDataVersion(),
                macroContext.dxyObservationDate(),
                macroContext.us10yYieldObservationDate(),
                macroContext.usdKrwObservationDate(),
                macroContext.dxyProxyValue(),
                macroContext.us10yYieldValue(),
                macroContext.usdKrwValue(),
                macroContext.comparisonFacts(),
                macroContext.windowSummaries(),
                macroHighlights(reportType, macroContext)
        );
    }

    AnalysisMacroWindowSummary primaryWindowSummary(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        if (macroContext.windowSummaries() == null || macroContext.windowSummaries().isEmpty()) {
            return null;
        }

        Map<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType, AnalysisMacroWindowSummary> summaryByType = macroContext.windowSummaries()
                                                                                                                                   .stream()
                                                                                                                                   .collect(Collectors.toMap(
                                                                                                                                           AnalysisMacroWindowSummary::windowType,
                                                                                                                                           summary -> summary,
                                                                                                                                           (left, right) -> left
                                                                                                                                   ));

        List<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType> priority = switch (reportType) {
            case SHORT_TERM -> List.of(
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_7D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_3D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_1D
            );
            case MID_TERM -> List.of(
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_14D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_7D
            );
            case LONG_TERM -> List.of(
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_180D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_90D,
                    com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D
            );
        };

        return priority.stream()
                       .map(summaryByType::get)
                       .filter(java.util.Objects::nonNull)
                       .findFirst()
                       .orElse(macroContext.windowSummaries().get(macroContext.windowSummaries().size() - 1));
    }

    AnalysisContextHeadlinePayload macroContextHeadline(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        if (macroContext == null) {
            return null;
        }

        if (macroContext.highlights() != null && !macroContext.highlights().isEmpty()) {
            AnalysisMacroHighlight highlight = macroContext.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.MACRO,
                    highlight.title(),
                    highlight.summary(),
                    headlineImportance(highlight.importance())
            );
        }

        AnalysisMacroComparisonFact primaryFact = primaryFact(reportType, macroContext);
        if (primaryFact == null) {
            return null;
        }

        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.MACRO,
                primaryFact.reference().name() + " macro shift",
                primaryFact.reference().name()
                        + " keeps DXY "
                        + formattingSupport.signedRatio(primaryFact.dxyProxyChangeRate())
                        + ", US10Y "
                        + formattingSupport.signedRatio(primaryFact.us10yYieldChangeRate())
                        + ", USD/KRW "
                        + formattingSupport.signedRatio(primaryFact.usdKrwChangeRate())
                        + ".",
                reportType == AnalysisReportType.SHORT_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    private AnalysisMacroComparisonFact primaryFact(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        if (macroContext.comparisonFacts() == null || macroContext.comparisonFacts().isEmpty()) {
            return null;
        }

        Map<AnalysisComparisonReference, AnalysisMacroComparisonFact> factByReference = macroContext.comparisonFacts()
                                                                                                   .stream()
                                                                                                   .collect(Collectors.toMap(
                                                                                                           AnalysisMacroComparisonFact::reference,
                                                                                                           fact -> fact,
                                                                                                           (left, right) -> left
                                                                                                   ));

        List<AnalysisComparisonReference> priority = switch (reportType) {
            case SHORT_TERM -> List.of(AnalysisComparisonReference.PREV_BATCH, AnalysisComparisonReference.D1, AnalysisComparisonReference.D7);
            case MID_TERM -> List.of(AnalysisComparisonReference.D7, AnalysisComparisonReference.D14, AnalysisComparisonReference.D30);
            case LONG_TERM -> List.of(AnalysisComparisonReference.D30, AnalysisComparisonReference.D90, AnalysisComparisonReference.D180);
        };

        return priority.stream()
                       .map(factByReference::get)
                       .filter(java.util.Objects::nonNull)
                       .findFirst()
                       .orElse(macroContext.comparisonFacts().get(0));
    }

    private List<AnalysisMacroHighlight> macroHighlights(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        ArrayList<AnalysisMacroHighlight> highlights = new ArrayList<>();

        AnalysisMacroComparisonFact primaryFact = primaryFact(reportType, macroContext);
        if (primaryFact != null) {
            if (primaryFact.dxyProxyChangeRate() != null && primaryFact.dxyProxyChangeRate().compareTo(new java.math.BigDecimal("0.01")) >= 0) {
                highlights.add(new AnalysisMacroHighlight(
                        "Dollar strength regime",
                        primaryFact.reference().name()
                                + " keeps DXY proxy "
                                + formattingSupport.signedRatio(primaryFact.dxyProxyChangeRate())
                                + ", which can pressure crypto risk appetite.",
                        AnalysisMacroHighlightImportance.HIGH,
                        primaryFact.reference()
                ));
            }

            if (primaryFact.us10yYieldChangeRate() != null && primaryFact.us10yYieldChangeRate().compareTo(new java.math.BigDecimal("0.03")) >= 0) {
                highlights.add(new AnalysisMacroHighlight(
                        "Yield pressure regime",
                        primaryFact.reference().name()
                                + " keeps US10Y yield "
                                + formattingSupport.signedRatio(primaryFact.us10yYieldChangeRate())
                                + ", which can tighten risk-sensitive positioning.",
                        AnalysisMacroHighlightImportance.MEDIUM,
                        primaryFact.reference()
                ));
            }

            if (primaryFact.usdKrwChangeRate() != null && primaryFact.usdKrwChangeRate().compareTo(new java.math.BigDecimal("0.01")) >= 0) {
                highlights.add(new AnalysisMacroHighlight(
                        "KRW weakness regime",
                        primaryFact.reference().name()
                                + " keeps USD/KRW "
                                + formattingSupport.signedRatio(primaryFact.usdKrwChangeRate())
                                + ", which points to a firmer dollar backdrop.",
                        AnalysisMacroHighlightImportance.MEDIUM,
                        primaryFact.reference()
                ));
            }
        }

        if (highlights.isEmpty() && primaryFact != null) {
            highlights.add(new AnalysisMacroHighlight(
                    primaryFact.reference().name() + " macro shift",
                    primaryFact.reference().name()
                            + " changes DXY "
                            + formattingSupport.signedRatio(primaryFact.dxyProxyChangeRate())
                            + ", US10Y "
                            + formattingSupport.signedRatio(primaryFact.us10yYieldChangeRate())
                            + ", USD/KRW "
                            + formattingSupport.signedRatio(primaryFact.usdKrwChangeRate())
                            + ".",
                    AnalysisMacroHighlightImportance.MEDIUM,
                    primaryFact.reference()
            ));
        }

        AnalysisMacroWindowSummary primaryWindowSummary = primaryWindowSummary(reportType, macroContext);
        if (primaryWindowSummary != null) {
            if (primaryWindowSummary.currentDxyProxyVsAverage() != null
                    && primaryWindowSummary.currentDxyProxyVsAverage().compareTo(new java.math.BigDecimal("0.01")) >= 0) {
                highlights.add(new AnalysisMacroHighlight(
                        primaryWindowSummary.windowType().name() + " dollar above average",
                        primaryWindowSummary.windowType().name()
                                + " keeps DXY proxy "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentDxyProxyVsAverage())
                                + " versus its average.",
                        AnalysisMacroHighlightImportance.MEDIUM,
                        null
                ));
            }
            if (primaryWindowSummary.currentUs10yYieldVsAverage() != null
                    && primaryWindowSummary.currentUs10yYieldVsAverage().compareTo(new java.math.BigDecimal("0.03")) >= 0) {
                highlights.add(new AnalysisMacroHighlight(
                        primaryWindowSummary.windowType().name() + " yield above average",
                        primaryWindowSummary.windowType().name()
                                + " keeps US10Y "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentUs10yYieldVsAverage())
                                + " versus its average.",
                        AnalysisMacroHighlightImportance.MEDIUM,
                        null
                ));
            }
        }

        return highlights.stream().limit(2).toList();
    }

    List<AnalysisExternalRegimeSignal> regimeSignals(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext
    ) {
        if (macroContext == null) {
            return List.of();
        }

        java.util.ArrayList<AnalysisExternalRegimeSignal> signals = new java.util.ArrayList<>();
        AnalysisMacroWindowSummary windowSummary = primaryWindowSummary(reportType, macroContext);
        if (windowSummary == null) {
            return List.of();
        }

        if (windowSummary.currentDxyProxyVsAverage().compareTo(new java.math.BigDecimal("0.01")) >= 0) {
            signals.add(new AnalysisExternalRegimeSignal(
                    AnalysisExternalRegimeCategory.MACRO,
                    "Dollar strength",
                    windowSummary.windowType().name()
                            + " DXY stays "
                            + formattingSupport.signedRatio(windowSummary.currentDxyProxyVsAverage())
                            + " versus average.",
                    AnalysisExternalRegimeDirection.HEADWIND,
                    AnalysisExternalRegimeSeverity.HIGH,
                    windowSummary.windowType().name()
            ));
        } else if (windowSummary.currentDxyProxyVsAverage().compareTo(new java.math.BigDecimal("-0.01")) <= 0) {
            signals.add(new AnalysisExternalRegimeSignal(
                    AnalysisExternalRegimeCategory.MACRO,
                    "Dollar easing",
                    windowSummary.windowType().name()
                            + " DXY stays "
                            + formattingSupport.signedRatio(windowSummary.currentDxyProxyVsAverage())
                            + " versus average.",
                    AnalysisExternalRegimeDirection.SUPPORTIVE,
                    AnalysisExternalRegimeSeverity.MEDIUM,
                    windowSummary.windowType().name()
            ));
        }

        if (windowSummary.currentUs10yYieldVsAverage().compareTo(new java.math.BigDecimal("0.03")) >= 0) {
            signals.add(new AnalysisExternalRegimeSignal(
                    AnalysisExternalRegimeCategory.MACRO,
                    "Yield pressure",
                    windowSummary.windowType().name()
                            + " US10Y stays "
                            + formattingSupport.signedRatio(windowSummary.currentUs10yYieldVsAverage())
                            + " versus average.",
                    AnalysisExternalRegimeDirection.HEADWIND,
                    AnalysisExternalRegimeSeverity.MEDIUM,
                    windowSummary.windowType().name()
            ));
        }

        return signals;
    }

    private AnalysisContextHeadlineImportance headlineImportance(AnalysisMacroHighlightImportance importance) {
        return switch (importance) {
            case HIGH -> AnalysisContextHeadlineImportance.HIGH;
            case MEDIUM -> AnalysisContextHeadlineImportance.MEDIUM;
        };
    }
}

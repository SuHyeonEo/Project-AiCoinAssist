package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisDerivativeContextSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisDerivativeContextSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    AnalysisDerivativeContext enrichDerivativeContext(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        if (derivativeContext == null) {
            return null;
        }

        return new AnalysisDerivativeContext(
                derivativeContext.snapshotTime(),
                derivativeContext.openInterestSourceEventTime(),
                derivativeContext.premiumIndexSourceEventTime(),
                derivativeContext.sourceDataVersion(),
                derivativeContext.openInterest(),
                derivativeContext.markPrice(),
                derivativeContext.indexPrice(),
                derivativeContext.lastFundingRate(),
                derivativeContext.nextFundingTime(),
                derivativeContext.markIndexBasisRate(),
                derivativeContext.comparisonFacts(),
                derivativeContext.windowSummaries(),
                derivativeHighlights(reportType, derivativeContext)
        );
    }

    AnalysisContextHeadlinePayload derivativeContextHeadline(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        if (derivativeContext == null) {
            return null;
        }
        if (derivativeContext.highlights() != null && !derivativeContext.highlights().isEmpty()) {
            AnalysisDerivativeHighlight highlight = derivativeContext.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.DERIVATIVE,
                    highlight.title(),
                    highlight.summary(),
                    headlineImportance(highlight.importance())
            );
        }

        AnalysisDerivativeComparisonFact primaryDerivativeFact = primaryDerivativeFact(reportType, derivativeContext);
        if (primaryDerivativeFact == null) {
            return null;
        }

        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.DERIVATIVE,
                primaryDerivativeFact.reference().name() + " derivative shift",
                primaryDerivativeFact.reference().name()
                        + " keeps OI "
                        + formattingSupport.signedRatio(primaryDerivativeFact.openInterestChangeRate())
                        + " with funding Δ "
                        + formattingSupport.fundingRatePercentage(primaryDerivativeFact.fundingRateDelta())
                        + ".",
                reportType == AnalysisReportType.SHORT_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    AnalysisDerivativeWindowSummary primaryDerivativeWindowSummary(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        if (derivativeContext.windowSummaries() == null || derivativeContext.windowSummaries().isEmpty()) {
            return null;
        }

        Map<MarketWindowType, AnalysisDerivativeWindowSummary> summaryByType = derivativeContext.windowSummaries()
                                                                                                .stream()
                                                                                                .collect(Collectors.toMap(
                                                                                                        AnalysisDerivativeWindowSummary::windowType,
                                                                                                        summary -> summary,
                                                                                                        (left, right) -> left
                                                                                                ));

        List<MarketWindowType> priority = switch (reportType) {
            case SHORT_TERM -> List.of(MarketWindowType.LAST_7D, MarketWindowType.LAST_3D, MarketWindowType.LAST_1D);
            case MID_TERM -> List.of(MarketWindowType.LAST_30D, MarketWindowType.LAST_14D, MarketWindowType.LAST_7D);
            case LONG_TERM -> List.of(MarketWindowType.LAST_180D, MarketWindowType.LAST_90D, MarketWindowType.LAST_30D);
        };

        return priority.stream()
                       .map(summaryByType::get)
                       .filter(java.util.Objects::nonNull)
                       .findFirst()
                       .orElse(derivativeContext.windowSummaries().get(derivativeContext.windowSummaries().size() - 1));
    }

    long hoursUntilNextFunding(AnalysisDerivativeContext derivativeContext) {
        long durationSeconds = java.time.Duration.between(
                derivativeContext.snapshotTime(),
                derivativeContext.nextFundingTime()
        ).toSeconds();
        if (durationSeconds <= 0) {
            return 0;
        }

        return java.time.Duration.ofSeconds(durationSeconds).toHours();
    }

    private AnalysisDerivativeComparisonFact primaryDerivativeFact(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        if (derivativeContext.comparisonFacts() == null || derivativeContext.comparisonFacts().isEmpty()) {
            return null;
        }

        Map<AnalysisComparisonReference, AnalysisDerivativeComparisonFact> factByReference = derivativeContext.comparisonFacts()
                                                                                             .stream()
                                                                                             .collect(Collectors.toMap(
                                                                                                     AnalysisDerivativeComparisonFact::reference,
                                                                                                     fact -> fact,
                                                                                                     (left, right) -> left
                                                                                             ));

        List<AnalysisComparisonReference> priority = switch (reportType) {
            case SHORT_TERM -> List.of(AnalysisComparisonReference.PREV_BATCH, AnalysisComparisonReference.D1, AnalysisComparisonReference.D3);
            case MID_TERM -> List.of(AnalysisComparisonReference.D7, AnalysisComparisonReference.D14, AnalysisComparisonReference.D30);
            case LONG_TERM -> List.of(AnalysisComparisonReference.D180, AnalysisComparisonReference.D90, AnalysisComparisonReference.D30);
        };

        return priority.stream()
                       .map(factByReference::get)
                       .filter(java.util.Objects::nonNull)
                       .findFirst()
                       .orElse(derivativeContext.comparisonFacts().get(0));
    }

    private List<AnalysisDerivativeHighlight> derivativeHighlights(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        java.util.ArrayList<AnalysisDerivativeHighlight> highlights = new java.util.ArrayList<>();

        AnalysisDerivativeComparisonFact primaryComparisonFact = primaryDerivativeFact(reportType, derivativeContext);
        if (primaryComparisonFact != null) {
            highlights.add(new AnalysisDerivativeHighlight(
                    primaryComparisonFact.reference().name() + " derivative shift",
                    primaryComparisonFact.reference().name()
                            + " keeps OI "
                            + formattingSupport.signedRatio(primaryComparisonFact.openInterestChangeRate())
                            + ", funding Δ "
                            + formattingSupport.fundingRatePercentage(primaryComparisonFact.fundingRateDelta())
                            + ", basis Δ "
                            + formattingSupport.signedPercent(primaryComparisonFact.basisRateDelta()),
                    reportType == AnalysisReportType.SHORT_TERM
                            ? AnalysisDerivativeHighlightImportance.HIGH
                            : AnalysisDerivativeHighlightImportance.MEDIUM,
                    AnalysisDerivativeMetricType.OPEN_INTEREST,
                    primaryComparisonFact.reference(),
                    null
            ));
        }

        AnalysisDerivativeWindowSummary primaryWindowSummary = primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (primaryWindowSummary != null) {
            highlights.add(new AnalysisDerivativeHighlight(
                    primaryWindowSummary.windowType().name() + " derivative regime",
                    primaryWindowSummary.windowType().name()
                            + " keeps funding vs average "
                            + formattingSupport.signedRatio(primaryWindowSummary.currentFundingVsAverage())
                            + ", OI vs average "
                            + formattingSupport.signedRatio(primaryWindowSummary.currentOpenInterestVsAverage())
                            + ", basis vs average "
                            + formattingSupport.signedRatio(primaryWindowSummary.currentBasisVsAverage()),
                    reportType == AnalysisReportType.LONG_TERM
                            ? AnalysisDerivativeHighlightImportance.HIGH
                            : AnalysisDerivativeHighlightImportance.MEDIUM,
                    AnalysisDerivativeMetricType.FUNDING_RATE,
                    null,
                    primaryWindowSummary.windowType()
            ));
        }

        if (derivativeContext.lastFundingRate() != null
                && derivativeContext.lastFundingRate().abs().compareTo(new BigDecimal("0.0004")) >= 0) {
            highlights.add(new AnalysisDerivativeHighlight(
                    "Funding crowding",
                    "Current funding is " + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", which points to leveraged directional crowding.",
                    AnalysisDerivativeHighlightImportance.HIGH,
                    AnalysisDerivativeMetricType.FUNDING_RATE,
                    null,
                    null
            ));
        }

        return highlights.stream().limit(3).toList();
    }

    private AnalysisContextHeadlineImportance headlineImportance(AnalysisDerivativeHighlightImportance importance) {
        if (importance == null) {
            return AnalysisContextHeadlineImportance.MEDIUM;
        }

        return switch (importance) {
            case HIGH -> AnalysisContextHeadlineImportance.HIGH;
            case MEDIUM -> AnalysisContextHeadlineImportance.MEDIUM;
        };
    }
}

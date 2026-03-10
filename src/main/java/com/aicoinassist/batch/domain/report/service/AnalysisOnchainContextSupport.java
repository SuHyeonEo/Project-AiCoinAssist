package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOnchainHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisOnchainContextSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisOnchainContextSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    AnalysisOnchainContext enrichOnchainContext(
            AnalysisReportType reportType,
            AnalysisOnchainContext onchainContext
    ) {
        if (onchainContext == null) {
            return null;
        }

        return new AnalysisOnchainContext(
                onchainContext.snapshotTime(),
                onchainContext.activeAddressSourceEventTime(),
                onchainContext.transactionCountSourceEventTime(),
                onchainContext.marketCapSourceEventTime(),
                onchainContext.sourceDataVersion(),
                onchainContext.activeAddressCount(),
                onchainContext.transactionCount(),
                onchainContext.marketCapUsd(),
                onchainContext.comparisonFacts(),
                onchainContext.windowSummaries(),
                onchainHighlights(reportType, onchainContext)
        );
    }

    AnalysisOnchainWindowSummary primaryWindowSummary(
            AnalysisReportType reportType,
            AnalysisOnchainContext onchainContext
    ) {
        if (onchainContext.windowSummaries() == null || onchainContext.windowSummaries().isEmpty()) {
            return null;
        }

        Map<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType, AnalysisOnchainWindowSummary> summaryByType = onchainContext.windowSummaries()
                                                                                                                                         .stream()
                                                                                                                                         .collect(Collectors.toMap(
                                                                                                                                                 AnalysisOnchainWindowSummary::windowType,
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
                       .orElse(onchainContext.windowSummaries().get(onchainContext.windowSummaries().size() - 1));
    }

    AnalysisContextHeadlinePayload onchainContextHeadline(
            AnalysisReportType reportType,
            AnalysisOnchainContext onchainContext
    ) {
        if (onchainContext == null) {
            return null;
        }

        if (onchainContext.highlights() != null && !onchainContext.highlights().isEmpty()) {
            AnalysisOnchainHighlight highlight = onchainContext.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.ONCHAIN,
                    highlight.title(),
                    highlight.summary(),
                    headlineImportance(highlight.importance())
            );
        }

        AnalysisOnchainComparisonFact primaryFact = primaryFact(reportType, onchainContext);
        if (primaryFact == null) {
            return null;
        }

        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.ONCHAIN,
                primaryFact.reference().name() + " on-chain shift",
                primaryFact.reference().name()
                        + " keeps active addresses "
                        + formattingSupport.signedRatio(primaryFact.activeAddressChangeRate())
                        + " and transactions "
                        + formattingSupport.signedRatio(primaryFact.transactionCountChangeRate()) + ".",
                reportType == AnalysisReportType.SHORT_TERM
                        ? AnalysisContextHeadlineImportance.MEDIUM
                        : AnalysisContextHeadlineImportance.HIGH
        );
    }

    private AnalysisOnchainComparisonFact primaryFact(
            AnalysisReportType reportType,
            AnalysisOnchainContext onchainContext
    ) {
        if (onchainContext.comparisonFacts() == null || onchainContext.comparisonFacts().isEmpty()) {
            return null;
        }

        Map<AnalysisComparisonReference, AnalysisOnchainComparisonFact> factByReference = onchainContext.comparisonFacts()
                                                                                                      .stream()
                                                                                                      .collect(Collectors.toMap(
                                                                                                              AnalysisOnchainComparisonFact::reference,
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
                       .orElse(onchainContext.comparisonFacts().get(0));
    }

    private List<AnalysisOnchainHighlight> onchainHighlights(
            AnalysisReportType reportType,
            AnalysisOnchainContext onchainContext
    ) {
        java.util.ArrayList<AnalysisOnchainHighlight> highlights = new java.util.ArrayList<>();

        AnalysisOnchainComparisonFact primaryFact = primaryFact(reportType, onchainContext);
        if (primaryFact != null) {
            boolean contraction = isContraction(primaryFact);
            highlights.add(new AnalysisOnchainHighlight(
                    primaryFact.reference().name() + (contraction ? " activity contraction" : " activity expansion"),
                    primaryFact.reference().name()
                            + " keeps active addresses "
                            + formattingSupport.signedRatio(primaryFact.activeAddressChangeRate())
                            + ", transactions "
                            + formattingSupport.signedRatio(primaryFact.transactionCountChangeRate())
                            + ", market cap "
                            + formattingSupport.signedRatio(primaryFact.marketCapChangeRate())
                            + ".",
                    contraction ? AnalysisOnchainHighlightImportance.HIGH : AnalysisOnchainHighlightImportance.MEDIUM,
                    primaryFact.reference()
            ));
        }

        if (onchainContext.marketCapUsd().compareTo(new java.math.BigDecimal("1000000000000")) >= 0) {
            highlights.add(new AnalysisOnchainHighlight(
                    "Large-cap on-chain base",
                    "On-chain market cap sits at "
                            + onchainContext.marketCapUsd().stripTrailingZeros().toPlainString()
                            + ", which keeps the asset in a large-cap network regime.",
                    AnalysisOnchainHighlightImportance.MEDIUM,
                    null
            ));
        }

        AnalysisOnchainWindowSummary primaryWindowSummary = primaryWindowSummary(reportType, onchainContext);
        if (primaryWindowSummary != null
                && primaryWindowSummary.currentActiveAddressVsAverage() != null
                && primaryWindowSummary.currentTransactionCountVsAverage() != null) {
            boolean expansion = primaryWindowSummary.currentActiveAddressVsAverage().compareTo(new java.math.BigDecimal("0.10")) >= 0
                    && primaryWindowSummary.currentTransactionCountVsAverage().compareTo(new java.math.BigDecimal("0.10")) >= 0;
            boolean contraction = primaryWindowSummary.currentActiveAddressVsAverage().compareTo(new java.math.BigDecimal("-0.10")) <= 0
                    && primaryWindowSummary.currentTransactionCountVsAverage().compareTo(new java.math.BigDecimal("-0.10")) <= 0;
            if (expansion || contraction) {
                highlights.add(new AnalysisOnchainHighlight(
                        primaryWindowSummary.windowType().name() + (expansion ? " network activity expansion" : " network activity contraction"),
                        primaryWindowSummary.windowType().name()
                                + " keeps active addresses "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentActiveAddressVsAverage())
                                + ", transactions "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentTransactionCountVsAverage())
                                + ", market cap "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentMarketCapVsAverage())
                                + " versus average.",
                        contraction ? AnalysisOnchainHighlightImportance.HIGH : AnalysisOnchainHighlightImportance.MEDIUM,
                        null
                ));
            }
        }

        return highlights.stream().limit(2).toList();
    }

    private boolean isContraction(AnalysisOnchainComparisonFact fact) {
        return fact.activeAddressChangeRate() != null
                && fact.transactionCountChangeRate() != null
                && fact.activeAddressChangeRate().compareTo(new java.math.BigDecimal("-0.05")) <= 0
                && fact.transactionCountChangeRate().compareTo(new java.math.BigDecimal("-0.05")) <= 0;
    }

    private AnalysisContextHeadlineImportance headlineImportance(AnalysisOnchainHighlightImportance importance) {
        return switch (importance) {
            case HIGH -> AnalysisContextHeadlineImportance.HIGH;
            case MEDIUM -> AnalysisContextHeadlineImportance.MEDIUM;
        };
    }
}

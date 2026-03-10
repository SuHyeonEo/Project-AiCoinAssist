package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisSentimentHighlightImportance;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisSentimentContextSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisSentimentContextSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    AnalysisSentimentContext enrichSentimentContext(
            AnalysisReportType reportType,
            AnalysisSentimentContext sentimentContext
    ) {
        if (sentimentContext == null) {
            return null;
        }

        return new AnalysisSentimentContext(
                sentimentContext.snapshotTime(),
                sentimentContext.sourceEventTime(),
                sentimentContext.sourceDataVersion(),
                sentimentContext.indexValue(),
                sentimentContext.classification(),
                sentimentContext.timeUntilUpdateSeconds(),
                sentimentContext.comparisonFacts(),
                sentimentContext.windowSummaries(),
                sentimentHighlights(reportType, sentimentContext)
        );
    }

    AnalysisSentimentWindowSummary primaryWindowSummary(
            AnalysisReportType reportType,
            AnalysisSentimentContext sentimentContext
    ) {
        if (sentimentContext.windowSummaries() == null || sentimentContext.windowSummaries().isEmpty()) {
            return null;
        }

        Map<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType, AnalysisSentimentWindowSummary> summaryByType = sentimentContext.windowSummaries()
                                                                                                                                            .stream()
                                                                                                                                            .collect(Collectors.toMap(
                                                                                                                                                    AnalysisSentimentWindowSummary::windowType,
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
                       .orElse(sentimentContext.windowSummaries().get(sentimentContext.windowSummaries().size() - 1));
    }

    AnalysisContextHeadlinePayload sentimentContextHeadline(
            AnalysisReportType reportType,
            AnalysisSentimentContext sentimentContext
    ) {
        if (sentimentContext == null) {
            return null;
        }

        if (sentimentContext.highlights() != null && !sentimentContext.highlights().isEmpty()) {
            AnalysisSentimentHighlight highlight = sentimentContext.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.SENTIMENT,
                    highlight.title(),
                    highlight.summary(),
                    headlineImportance(highlight.importance())
            );
        }

        AnalysisSentimentComparisonFact primaryFact = primaryFact(reportType, sentimentContext);
        if (primaryFact == null) {
            return null;
        }

        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.SENTIMENT,
                primaryFact.reference().name() + " sentiment shift",
                primaryFact.reference().name()
                        + " keeps Fear & Greed at "
                        + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                        + " with Δ "
                        + primaryFact.valueChange().stripTrailingZeros().toPlainString() + ".",
                reportType == AnalysisReportType.SHORT_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    long hoursUntilNextUpdate(AnalysisSentimentContext sentimentContext) {
        if (sentimentContext == null || sentimentContext.timeUntilUpdateSeconds() == null) {
            return 0L;
        }
        return Duration.ofSeconds(sentimentContext.timeUntilUpdateSeconds()).toHours();
    }

    private AnalysisSentimentComparisonFact primaryFact(
            AnalysisReportType reportType,
            AnalysisSentimentContext sentimentContext
    ) {
        if (sentimentContext.comparisonFacts() == null || sentimentContext.comparisonFacts().isEmpty()) {
            return null;
        }

        Map<AnalysisComparisonReference, AnalysisSentimentComparisonFact> factByReference = sentimentContext.comparisonFacts()
                                                                                                           .stream()
                                                                                                           .collect(Collectors.toMap(
                                                                                                                   AnalysisSentimentComparisonFact::reference,
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
                       .orElse(sentimentContext.comparisonFacts().get(0));
    }

    private List<AnalysisSentimentHighlight> sentimentHighlights(
            AnalysisReportType reportType,
            AnalysisSentimentContext sentimentContext
    ) {
        java.util.ArrayList<AnalysisSentimentHighlight> highlights = new java.util.ArrayList<>();

        if ("Extreme Greed".equalsIgnoreCase(sentimentContext.classification())
                || "Greed".equalsIgnoreCase(sentimentContext.classification())) {
            highlights.add(new AnalysisSentimentHighlight(
                    "Greed regime",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), which points to risk appetite staying elevated.",
                    AnalysisSentimentHighlightImportance.HIGH,
                    null
            ));
        } else if ("Extreme Fear".equalsIgnoreCase(sentimentContext.classification())
                || "Fear".equalsIgnoreCase(sentimentContext.classification())) {
            highlights.add(new AnalysisSentimentHighlight(
                    "Fear regime",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), which points to defensive positioning staying elevated.",
                    AnalysisSentimentHighlightImportance.HIGH,
                    null
            ));
        }

        AnalysisSentimentComparisonFact primaryFact = primaryFact(reportType, sentimentContext);
        if (primaryFact != null) {
            highlights.add(new AnalysisSentimentHighlight(
                    primaryFact.reference().name() + " sentiment shift",
                    primaryFact.reference().name()
                            + " changes Fear & Greed by "
                            + primaryFact.valueChange().stripTrailingZeros().toPlainString()
                            + " ("
                            + formattingSupport.signedRatio(primaryFact.valueChangeRate())
                            + ")"
                            + (Boolean.TRUE.equals(primaryFact.classificationChanged())
                            ? " with classification switching from " + primaryFact.referenceClassification() + " to " + sentimentContext.classification() + "."
                            : " while classification stays " + sentimentContext.classification() + "."),
                    reportType == AnalysisReportType.SHORT_TERM
                            ? AnalysisSentimentHighlightImportance.HIGH
                            : AnalysisSentimentHighlightImportance.MEDIUM,
                    primaryFact.reference()
            ));
        }

        AnalysisSentimentWindowSummary primaryWindowSummary = primaryWindowSummary(reportType, sentimentContext);
        if (primaryWindowSummary != null && primaryWindowSummary.currentIndexVsAverage() != null) {
            if (primaryWindowSummary.currentIndexVsAverage().compareTo(new java.math.BigDecimal("0.10")) >= 0) {
                highlights.add(new AnalysisSentimentHighlight(
                        primaryWindowSummary.windowType().name() + " sentiment above average",
                        primaryWindowSummary.windowType().name()
                                + " keeps Fear & Greed "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentIndexVsAverage())
                                + " versus its average, with greed samples "
                                + primaryWindowSummary.greedSampleCount()
                                + " out of "
                                + primaryWindowSummary.sampleCount()
                                + ".",
                        AnalysisSentimentHighlightImportance.MEDIUM,
                        null
                ));
            } else if (primaryWindowSummary.currentIndexVsAverage().compareTo(new java.math.BigDecimal("-0.10")) <= 0) {
                highlights.add(new AnalysisSentimentHighlight(
                        primaryWindowSummary.windowType().name() + " sentiment below average",
                        primaryWindowSummary.windowType().name()
                                + " keeps Fear & Greed "
                                + formattingSupport.signedRatio(primaryWindowSummary.currentIndexVsAverage())
                                + " versus its average, with fear samples "
                                + primaryWindowSummary.fearSampleCount()
                                + " out of "
                                + primaryWindowSummary.sampleCount()
                                + ".",
                        AnalysisSentimentHighlightImportance.MEDIUM,
                        null
                ));
            }
        }

        return highlights.stream().limit(2).toList();
    }

    private AnalysisContextHeadlineImportance headlineImportance(AnalysisSentimentHighlightImportance importance) {
        return switch (importance) {
            case HIGH -> AnalysisContextHeadlineImportance.HIGH;
            case MEDIUM -> AnalysisContextHeadlineImportance.MEDIUM;
        };
    }
}

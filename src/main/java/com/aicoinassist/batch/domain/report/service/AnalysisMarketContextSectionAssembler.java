package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFactSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AnalysisMarketContextSectionAssembler {

    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisComparisonWindowSupport comparisonWindowSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;
    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisMarketContextSectionAssembler(
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

    AnalysisMarketContextPayload buildMarketContext(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowHighlight> windowHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            List<AnalysisContinuityNote> continuityNotes,
            AnalysisLevelContextPayload levelContext,
            List<AnalysisRiskFactor> riskFactors
    ) {
        List<AnalysisMovingAveragePositionPayload> movingAveragePositions = List.of(
                indicatorStateSupport.movingAveragePosition(snapshot.getCurrentPrice(), snapshot.getMa20(), "MA20"),
                indicatorStateSupport.movingAveragePosition(snapshot.getCurrentPrice(), snapshot.getMa60(), "MA60"),
                indicatorStateSupport.movingAveragePosition(snapshot.getCurrentPrice(), snapshot.getMa120(), "MA120")
        );
        AnalysisComparisonFactSummaryPayload comparisonSummary = new AnalysisComparisonFactSummaryPayload(
                comparisonFacts.isEmpty()
                        ? "No comparison facts available."
                        : comparisonWindowSupport.comparisonFactSummary(comparisonFacts.get(0)),
                comparisonFacts.stream()
                               .skip(1)
                               .map(comparisonWindowSupport::comparisonFactSummary)
                               .toList()
        );
        List<String> comparisonHighlightDetails = comparisonHighlights.stream()
                                                                     .map(AnalysisComparisonHighlight::detail)
                                                                     .collect(Collectors.toCollection(ArrayList::new));
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .forEach(comparisonHighlightDetails::add);
        AnalysisContextHeadlinePayload comparisonHeadline = comparisonWindowSupport.comparisonContextHeadline(
                reportType,
                comparisonFacts,
                comparisonHighlights
        );

        AnalysisWindowSummary primaryWindow = comparisonWindowSupport.primaryWindow(windowSummaries);
        AnalysisWindowContextSummaryPayload windowSummary = null;
        if (primaryWindow != null) {
            windowSummary = new AnalysisWindowContextSummaryPayload(
                    primaryWindow.windowType().name()
                            + " range "
                            + primaryWindow.low().stripTrailingZeros().toPlainString()
                            + " to "
                            + primaryWindow.high().stripTrailingZeros().toPlainString()
                            + ".",
                    primaryWindow.windowType().name()
                            + " position "
                            + formattingSupport.percentage(primaryWindow.currentPositionInRange())
                            + " with distance from high "
                            + formattingSupport.percentage(primaryWindow.distanceFromWindowHigh())
                            + ".",
                    "ATR vs average " + formattingSupport.signedRatio(primaryWindow.currentAtrVsAverage()) + "."
            );
        }
        List<String> windowHighlightDetails = windowHighlights.stream()
                                                              .map(AnalysisWindowHighlight::detail)
                                                              .collect(Collectors.toCollection(ArrayList::new));
        levelContext.zoneInteractionFacts().stream()
                    .map(AnalysisZoneInteractionFact::summary)
                    .forEach(windowHighlightDetails::add);
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .forEach(windowHighlightDetails::add);
        AnalysisContextHeadlinePayload windowHeadline = comparisonWindowSupport.windowContextHeadline(reportType, windowSummaries);

        AnalysisDerivativeContextSummaryPayload derivativeContextSummary = null;
        AnalysisContextHeadlinePayload derivativeHeadline = null;
        AnalysisMacroContextSummaryPayload macroContextSummary = null;
        AnalysisContextHeadlinePayload macroHeadline = null;
        AnalysisSentimentContextSummaryPayload sentimentContextSummary = null;
        AnalysisContextHeadlinePayload sentimentHeadline = null;
        AnalysisOnchainContextSummaryPayload onchainContextSummary = null;
        AnalysisContextHeadlinePayload onchainHeadline = null;
        if (derivativeContext != null) {
            AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContextSupport.primaryDerivativeWindowSummary(
                    reportType,
                    derivativeContext
            );
            derivativeContextSummary = new AnalysisDerivativeContextSummaryPayload(
                    "Open interest "
                            + derivativeContext.openInterest().stripTrailingZeros().toPlainString()
                            + ", funding "
                            + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", basis "
                            + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate())
                            + ", mark-index spread "
                            + derivativeContext.markPrice()
                                               .subtract(derivativeContext.indexPrice())
                                               .stripTrailingZeros()
                                               .toPlainString()
                            + ".",
                    derivativeWindowSummary == null
                            ? null
                            : derivativeWindowSummary.windowType().name()
                                    + " OI vs average "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                                    + ", funding vs average "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentFundingVsAverage())
                                    + ", basis vs average "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentBasisVsAverage())
                                    + ".",
                    derivativeContext.highlights() == null
                            ? List.of()
                            : derivativeContext.highlights().stream()
                                               .map(AnalysisDerivativeHighlight::summary)
                                               .toList(),
                    riskFactors.stream().map(AnalysisRiskFactor::title).toList(),
                    derivativeContextSupport.hoursUntilNextFunding(derivativeContext)
            );
            derivativeHeadline = derivativeContextSupport.derivativeContextHeadline(reportType, derivativeContext);
        }
        if (macroContext != null) {
            AnalysisMacroHighlight primaryHighlight = macroContext.highlights() == null || macroContext.highlights().isEmpty()
                    ? null
                    : macroContext.highlights().get(0);
            macroContextSummary = new AnalysisMacroContextSummaryPayload(
                    "DXY proxy "
                            + macroContext.dxyProxyValue().stripTrailingZeros().toPlainString()
                            + ", US10Y "
                            + macroContext.us10yYieldValue().stripTrailingZeros().toPlainString()
                            + ", USD/KRW "
                            + macroContext.usdKrwValue().stripTrailingZeros().toPlainString()
                            + ".",
                    primaryHighlight == null ? null : primaryHighlight.summary(),
                    macroContext.highlights() == null
                            ? List.of()
                            : macroContext.highlights().stream()
                                          .map(AnalysisMacroHighlight::summary)
                                          .toList()
            );
            macroHeadline = macroContextSupport.macroContextHeadline(reportType, macroContext);
        }
        if (sentimentContext != null) {
            AnalysisSentimentHighlight primaryHighlight = sentimentContext.highlights() == null || sentimentContext.highlights().isEmpty()
                    ? null
                    : sentimentContext.highlights().get(0);
            sentimentContextSummary = new AnalysisSentimentContextSummaryPayload(
                    "Fear & Greed "
                            + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " ("
                            + sentimentContext.classification()
                            + ").",
                    primaryHighlight == null ? null : primaryHighlight.summary(),
                    sentimentContext.highlights() == null
                            ? List.of()
                            : sentimentContext.highlights().stream()
                                              .map(AnalysisSentimentHighlight::summary)
                                              .toList(),
                    sentimentContextSupport.hoursUntilNextUpdate(sentimentContext)
            );
            sentimentHeadline = sentimentContextSupport.sentimentContextHeadline(reportType, sentimentContext);
        }
        if (onchainContext != null) {
            AnalysisOnchainHighlight primaryHighlight = onchainContext.highlights() == null || onchainContext.highlights().isEmpty()
                    ? null
                    : onchainContext.highlights().get(0);
            onchainContextSummary = new AnalysisOnchainContextSummaryPayload(
                    "Active addresses "
                            + onchainContext.activeAddressCount().stripTrailingZeros().toPlainString()
                            + ", transactions "
                            + onchainContext.transactionCount().stripTrailingZeros().toPlainString()
                            + ", market cap "
                            + onchainContext.marketCapUsd().stripTrailingZeros().toPlainString()
                            + ".",
                    primaryHighlight == null ? null : primaryHighlight.summary(),
                    onchainContext.highlights() == null
                            ? List.of()
                            : onchainContext.highlights().stream()
                                            .map(AnalysisOnchainHighlight::summary)
                                            .toList()
            );
            onchainHeadline = onchainContextSupport.onchainContextHeadline(reportType, onchainContext);
        }

        AnalysisContinuityContextPayload continuityContext = continuityNotes.isEmpty()
                ? null
                : new AnalysisContinuityContextPayload(
                        continuityNotes.get(0).reference(),
                        continuityNotes.get(0).summary(),
                        List.of(continuityNotes.get(0).summary()),
                        List.of()
                );

        return new AnalysisMarketContextPayload(
                new AnalysisCurrentStatePayload(
                        snapshot.getCurrentPrice(),
                        trendBias,
                        indicatorStateSupport.volatilityLabel(snapshot),
                        indicatorStateSupport.rangePositionLabel(primaryWindow),
                        movingAveragePositions,
                        indicatorStateSupport.momentumState(snapshot)
                ),
                new AnalysisComparisonContextPayload(
                        comparisonHeadline,
                        comparisonSummary,
                        comparisonHighlightDetails
                ),
                new AnalysisWindowContextPayload(
                        windowHeadline,
                        windowSummary,
                        windowHighlightDetails
                ),
                levelContext,
                derivativeContextSummary,
                derivativeHeadline,
                macroContextSummary,
                macroHeadline,
                sentimentContextSummary,
                sentimentHeadline,
                onchainContextSummary,
                onchainHeadline,
                continuityContext
        );
    }
}

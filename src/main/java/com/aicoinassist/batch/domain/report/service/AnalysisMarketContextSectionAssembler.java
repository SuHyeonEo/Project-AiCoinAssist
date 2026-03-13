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
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
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
    private final AnalysisTextLocalizationSupport textLocalizationSupport;

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
        this.textLocalizationSupport = new AnalysisTextLocalizationSupport();
    }

    AnalysisMarketContextPayload buildMarketContext(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowHighlight> windowHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            List<String> marketParticipationFacts,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            List<AnalysisContinuityNote> continuityNotes,
            AnalysisExternalContextCompositePayload externalContextComposite,
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
                        ? "비교 팩트가 없습니다."
                        : textLocalizationSupport.localizeSentence(comparisonWindowSupport.comparisonFactSummary(comparisonFacts.get(0))),
                comparisonFacts.stream()
                               .skip(1)
                               .map(comparisonWindowSupport::comparisonFactSummary)
                               .map(textLocalizationSupport::localizeSentence)
                               .toList()
        );
        List<String> comparisonHighlightDetails = comparisonHighlights.stream()
                                                                     .map(AnalysisComparisonHighlight::detail)
                                                                     .map(textLocalizationSupport::localizeSentence)
                                                                     .collect(Collectors.toCollection(ArrayList::new));
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .map(textLocalizationSupport::localizeSentence)
                    .forEach(comparisonHighlightDetails::add);
        AnalysisContextHeadlinePayload comparisonHeadline = localizeHeadline(comparisonWindowSupport.comparisonContextHeadline(
                reportType,
                comparisonFacts,
                comparisonHighlights
        ));

        AnalysisWindowSummary primaryWindow = comparisonWindowSupport.primaryWindow(windowSummaries);
        AnalysisWindowContextSummaryPayload windowSummary = null;
        if (primaryWindow != null) {
            windowSummary = new AnalysisWindowContextSummaryPayload(
                    textLocalizationSupport.windowLabel(primaryWindow.windowType())
                            + " 레인지는 "
                            + formattingSupport.plain(primaryWindow.low())
                            + "부터 "
                            + formattingSupport.plain(primaryWindow.high())
                            + "까지입니다.",
                    textLocalizationSupport.windowLabel(primaryWindow.windowType())
                            + " 기준 현재 위치는 레인지의 "
                            + formattingSupport.percentage(primaryWindow.currentPositionInRange())
                            + "이며, 고점 대비 거리는 "
                            + formattingSupport.percentage(primaryWindow.distanceFromWindowHigh())
                            + "입니다.",
                    String.join(" ", gather(
                            "거래량은 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindow.currentVolumeVsAverage())
                                    + "입니다.",
                            primaryWindow.currentQuoteAssetVolumeVsAverage() == null
                                    ? null
                                    : "거래대금은 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindow.currentQuoteAssetVolumeVsAverage())
                                    + "입니다.",
                            primaryWindow.currentTradeCountVsAverage() == null
                                    ? null
                                    : "체결 수는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindow.currentTradeCountVsAverage())
                                    + "입니다.",
                            primaryWindow.currentTakerBuyQuoteRatio() == null
                                    ? null
                                    : "taker buy 비중은 "
                                    + formattingSupport.percentage(primaryWindow.currentTakerBuyQuoteRatio())
                                    + "입니다.",
                            "ATR은 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindow.currentAtrVsAverage())
                                    + "입니다."
                    ))
            );
        }
        List<String> windowHighlightDetails = windowHighlights.stream()
                                                              .map(AnalysisWindowHighlight::detail)
                                                              .map(textLocalizationSupport::localizeSentence)
                                                              .collect(Collectors.toCollection(ArrayList::new));
        if (marketParticipationFacts != null) {
            marketParticipationFacts.stream()
                                    .map(textLocalizationSupport::localizeSentence)
                                    .forEach(windowHighlightDetails::add);
        }
        levelContext.zoneInteractionFacts().stream()
                    .map(AnalysisZoneInteractionFact::summary)
                    .map(textLocalizationSupport::localizeSentence)
                    .forEach(windowHighlightDetails::add);
        levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .map(textLocalizationSupport::localizeSentence)
                    .forEach(windowHighlightDetails::add);
        AnalysisContextHeadlinePayload windowHeadline = localizeHeadline(comparisonWindowSupport.windowContextHeadline(reportType, windowSummaries));

        AnalysisDerivativeContextSummaryPayload derivativeContextSummary = null;
        AnalysisContextHeadlinePayload derivativeHeadline = null;
        AnalysisMacroContextSummaryPayload macroContextSummary = null;
        AnalysisContextHeadlinePayload macroHeadline = null;
        AnalysisSentimentContextSummaryPayload sentimentContextSummary = null;
        AnalysisContextHeadlinePayload sentimentHeadline = null;
        AnalysisOnchainContextSummaryPayload onchainContextSummary = null;
        AnalysisContextHeadlinePayload onchainHeadline = null;
        AnalysisContextHeadlinePayload externalHeadline = null;
        if (derivativeContext != null) {
            AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContextSupport.primaryDerivativeWindowSummary(
                    reportType,
                    derivativeContext
            );
            derivativeContextSummary = new AnalysisDerivativeContextSummaryPayload(
                    "미결제약정은 "
                            + formattingSupport.plain(derivativeContext.openInterest())
                            + ", 펀딩은 "
                            + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", 베이시스는 "
                            + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate())
                            + ", 마크-인덱스 스프레드는 "
                            + formattingSupport.plain(derivativeContext.markPrice().subtract(derivativeContext.indexPrice()))
                            + "입니다.",
                    derivativeWindowSummary == null
                            ? null
                            : textLocalizationSupport.windowLabel(derivativeWindowSummary.windowType())
                                    + " 기준 OI는 평균 대비 "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                                    + ", 펀딩은 평균 대비 "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentFundingVsAverage())
                                    + ", 베이시스는 평균 대비 "
                                    + formattingSupport.signedRatio(derivativeWindowSummary.currentBasisVsAverage())
                                    + "입니다.",
                    derivativeContext.highlights() == null
                            ? List.of()
                            : derivativeContext.highlights().stream()
                                               .map(AnalysisDerivativeHighlight::summary)
                                               .map(textLocalizationSupport::localizeSentence)
                                               .toList(),
                    riskFactors.stream().map(AnalysisRiskFactor::title).toList(),
                    derivativeContextSupport.hoursUntilNextFunding(derivativeContext)
            );
            derivativeHeadline = localizeHeadline(derivativeContextSupport.derivativeContextHeadline(reportType, derivativeContext));
        }
        if (macroContext != null) {
            AnalysisMacroHighlight primaryHighlight = macroContext.highlights() == null || macroContext.highlights().isEmpty()
                    ? null
                    : macroContext.highlights().get(0);
            AnalysisMacroWindowSummary primaryWindowSummary = macroContextSupport.primaryWindowSummary(reportType, macroContext);
            macroContextSummary = new AnalysisMacroContextSummaryPayload(
                    "DXY 프록시는 "
                            + formattingSupport.plain(macroContext.dxyProxyValue())
                            + ", US10Y는 "
                            + formattingSupport.plain(macroContext.us10yYieldValue())
                            + ", USD/KRW는 "
                            + formattingSupport.plain(macroContext.usdKrwValue())
                            + "입니다.",
                    primaryHighlight == null ? null : textLocalizationSupport.localizeSentence(primaryHighlight.summary()),
                    primaryWindowSummary == null
                            ? null
                            : textLocalizationSupport.windowLabel(primaryWindowSummary.windowType())
                                    + " 기준 DXY는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentDxyProxyVsAverage())
                                    + ", US10Y는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentUs10yYieldVsAverage())
                                    + ", USD/KRW는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentUsdKrwVsAverage())
                                    + "입니다.",
                    macroContext.highlights() == null
                            ? List.of()
                            : macroContext.highlights().stream()
                                          .map(AnalysisMacroHighlight::summary)
                                          .map(textLocalizationSupport::localizeSentence)
                                          .toList()
            );
            macroHeadline = localizeHeadline(macroContextSupport.macroContextHeadline(reportType, macroContext));
        }
        if (sentimentContext != null) {
            AnalysisSentimentHighlight primaryHighlight = sentimentContext.highlights() == null || sentimentContext.highlights().isEmpty()
                    ? null
                    : sentimentContext.highlights().get(0);
            AnalysisSentimentWindowSummary primaryWindowSummary = sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
            sentimentContextSummary = new AnalysisSentimentContextSummaryPayload(
                    "공포·탐욕 지수는 "
                            + formattingSupport.plain(sentimentContext.indexValue())
                            + " ("
                            + textLocalizationSupport.classificationLabel(sentimentContext.classification())
                            + ")입니다.",
                    primaryHighlight == null ? null : textLocalizationSupport.localizeSentence(primaryHighlight.summary()),
                    primaryWindowSummary == null
                            ? null
                            : textLocalizationSupport.windowLabel(primaryWindowSummary.windowType())
                                    + " 기준 공포·탐욕 지수는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentIndexVsAverage())
                                    + "이며, 탐욕 표본은 "
                                    + primaryWindowSummary.greedSampleCount()
                                    + "/"
                                    + primaryWindowSummary.sampleCount()
                                    + "입니다.",
                    sentimentContext.highlights() == null
                            ? List.of()
                            : sentimentContext.highlights().stream()
                                              .map(AnalysisSentimentHighlight::summary)
                                              .map(textLocalizationSupport::localizeSentence)
                                              .toList(),
                    sentimentContextSupport.hoursUntilNextUpdate(sentimentContext)
            );
            sentimentHeadline = localizeHeadline(sentimentContextSupport.sentimentContextHeadline(reportType, sentimentContext));
        }
        if (onchainContext != null) {
            AnalysisOnchainHighlight primaryHighlight = onchainContext.highlights() == null || onchainContext.highlights().isEmpty()
                    ? null
                    : onchainContext.highlights().get(0);
            AnalysisOnchainWindowSummary primaryWindowSummary = onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
            onchainContextSummary = new AnalysisOnchainContextSummaryPayload(
                    "활성 주소는 "
                            + formattingSupport.plain(onchainContext.activeAddressCount())
                            + ", 트랜잭션은 "
                            + formattingSupport.plain(onchainContext.transactionCount())
                            + ", 시가총액은 "
                            + formattingSupport.plain(onchainContext.marketCapUsd())
                            + "입니다.",
                    primaryHighlight == null ? null : textLocalizationSupport.localizeSentence(primaryHighlight.summary()),
                    primaryWindowSummary == null
                            ? null
                            : textLocalizationSupport.windowLabel(primaryWindowSummary.windowType())
                                    + " 기준 활성 주소는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentActiveAddressVsAverage())
                                    + ", 트랜잭션은 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentTransactionCountVsAverage())
                                    + ", 시가총액은 평균 대비 "
                                    + formattingSupport.signedRatio(primaryWindowSummary.currentMarketCapVsAverage())
                                    + "입니다.",
                    onchainContext.highlights() == null
                            ? List.of()
                            : onchainContext.highlights().stream()
                                            .map(AnalysisOnchainHighlight::summary)
                                            .map(textLocalizationSupport::localizeSentence)
                                            .toList()
            );
            onchainHeadline = localizeHeadline(onchainContextSupport.onchainContextHeadline(reportType, onchainContext));
        }
        List<AnalysisExternalRegimeSignal> externalRegimeSignals = externalContextComposite == null
                ? List.of()
                : externalContextComposite.regimeSignals();
        if (externalContextComposite != null) {
            externalHeadline = localizeHeadline(externalContextHeadline(externalContextComposite));
        }

        AnalysisContinuityContextPayload continuityContext = continuityNotes.isEmpty()
                ? null
                : new AnalysisContinuityContextPayload(
                        continuityNotes.get(0).reference(),
                        textLocalizationSupport.localizeSentence(continuityNotes.get(0).summary()),
                        List.of(textLocalizationSupport.localizeSentence(continuityNotes.get(0).summary())),
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
                externalHeadline,
                externalContextComposite,
                continuityContext,
                externalRegimeSignals
        );
    }

    private AnalysisContextHeadlinePayload externalContextHeadline(
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
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

    private AnalysisContextHeadlinePayload localizeHeadline(AnalysisContextHeadlinePayload headline) {
        if (headline == null) {
            return null;
        }
        return new AnalysisContextHeadlinePayload(
                headline.category(),
                headline.title(),
                textLocalizationSupport.localizeSentence(headline.detail()),
                headline.importance()
        );
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
}

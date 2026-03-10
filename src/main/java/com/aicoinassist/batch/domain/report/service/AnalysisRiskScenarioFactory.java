package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;

import java.math.BigDecimal;
import java.util.List;

class AnalysisRiskScenarioFactory {

    private final AnalysisReportFormattingSupport formattingSupport;
    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;

    AnalysisRiskScenarioFactory(
            AnalysisReportFormattingSupport formattingSupport,
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport,
            AnalysisMacroContextSupport macroContextSupport,
            AnalysisSentimentContextSupport sentimentContextSupport,
            AnalysisOnchainContextSupport onchainContextSupport
    ) {
        this.formattingSupport = formattingSupport;
        this.indicatorStateSupport = indicatorStateSupport;
        this.derivativeContextSupport = derivativeContextSupport;
        this.macroContextSupport = macroContextSupport;
        this.sentimentContextSupport = sentimentContextSupport;
        this.onchainContextSupport = onchainContextSupport;
    }

    List<AnalysisRiskFactor> riskFactors(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<AnalysisRiskFactor> candidates = new java.util.ArrayList<>();

        if (snapshot.getRsi14().compareTo(new BigDecimal("70")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_OVERHEATING,
                    "RSI overheating",
                    "RSI14 is above 70, so upside continuation can weaken quickly.",
                    List.of("RSI14 " + snapshot.getRsi14().stripTrailingZeros().toPlainString() + " is above the 70 threshold.")
            ));
        }

        if (snapshot.getRsi14().compareTo(new BigDecimal("30")) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_COMPRESSION,
                    "RSI compression",
                    "RSI14 is below 30, so downside can be stretched and whipsaws can increase.",
                    List.of("RSI14 " + snapshot.getRsi14().stripTrailingZeros().toPlainString() + " is below the 30 threshold.")
            ));
        }

        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerUpperBand()) >= 0
                || snapshot.getCurrentPrice().compareTo(snapshot.getBollingerLowerBand()) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BAND_EXTENSION,
                    "Band extension",
                    "Price is trading at an outer Bollinger band, which raises reversion risk.",
                    List.of("Current price is touching an outer Bollinger band.")
            ));
        }

        if (indicatorStateSupport.atrRatio(snapshot).compareTo(new BigDecimal("3.00")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.ELEVATED_VOLATILITY,
                    "Elevated volatility",
                    "ATR14 is more than 3% of price, so intraperiod swings can expand.",
                    List.of("ATR14 ratio is " + indicatorStateSupport.atrRatio(snapshot).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "% of price.")
            ));
        }

        if (derivativeContext != null && derivativeContext.lastFundingRate().abs().compareTo(new BigDecimal("0.0004")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.FUNDING_SKEW,
                    "Funding skew",
                    "Funding is running at " + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", which can signal crowded directional leverage.",
                    List.of("Current funding rate is " + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate()) + ".")
            ));
        }

        if (derivativeContext != null && derivativeContext.markIndexBasisRate().abs().compareTo(new BigDecimal("0.05")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BASIS_EXPANSION,
                    "Basis expansion",
                    "Mark/index basis is " + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate())
                            + ", so futures positioning is trading away from spot.",
                    List.of("Mark/index basis rate is " + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate()) + ".")
            ));
        }

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null
                && derivativeWindowSummary.currentOpenInterestVsAverage() != null
                && derivativeWindowSummary.currentOpenInterestVsAverage().abs().compareTo(new BigDecimal("0.20")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.OPEN_INTEREST_CROWDING,
                    "Open interest crowding",
                    "Open interest is running " + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                            + " versus the representative window average.",
                    List.of("Open interest vs average is " + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage()) + ".")
            ));
        }

        AnalysisSentimentWindowSummary primarySentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentContext != null && (
                sentimentContext.indexValue().compareTo(new BigDecimal("70")) >= 0
                        || (primarySentimentWindowSummary != null
                        && primarySentimentWindowSummary.currentIndexVsAverage() != null
                        && primarySentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("0.15")) >= 0)
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_GREED_EXTREME,
                    "Sentiment greed extreme",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), so chase risk can rise near resistance.",
                    List.of(
                            "Fear & Greed classification is " + sentimentContext.classification() + ".",
                            primarySentimentWindowSummary == null || primarySentimentWindowSummary.currentIndexVsAverage() == null
                                    ? "Current sentiment is elevated."
                                    : primarySentimentWindowSummary.windowType().name()
                                    + " sentiment vs average is "
                                    + formattingSupport.signedRatio(primarySentimentWindowSummary.currentIndexVsAverage()) + "."
                    )
            ));
        }

        if (sentimentContext != null && (
                sentimentContext.indexValue().compareTo(new BigDecimal("30")) <= 0
                        || (primarySentimentWindowSummary != null
                        && primarySentimentWindowSummary.currentIndexVsAverage() != null
                        && primarySentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("-0.15")) <= 0)
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_FEAR_EXTREME,
                    "Sentiment fear extreme",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), so reactive selloffs and whipsaws can expand.",
                    List.of(
                            "Fear & Greed classification is " + sentimentContext.classification() + ".",
                            primarySentimentWindowSummary == null || primarySentimentWindowSummary.currentIndexVsAverage() == null
                                    ? "Current sentiment is depressed."
                                    : primarySentimentWindowSummary.windowType().name()
                                    + " sentiment vs average is "
                                    + formattingSupport.signedRatio(primarySentimentWindowSummary.currentIndexVsAverage()) + "."
                    )
            ));
        }

        AnalysisMacroWindowSummary primaryMacroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroContext != null && (
                macroContext.dxyProxyValue().compareTo(new BigDecimal("120")) >= 0
                        || macroContext.us10yYieldValue().compareTo(new BigDecimal("4.50")) >= 0
                        || macroContext.usdKrwValue().compareTo(new BigDecimal("1450")) >= 0
                        || (primaryMacroWindowSummary != null
                        && (
                        primaryMacroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                                || primaryMacroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
                                || primaryMacroWindowSummary.currentUsdKrwVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                ))
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.MACRO_VOLATILITY,
                    "Macro volatility",
                    "Macro backdrop is firm with DXY "
                            + macroContext.dxyProxyValue().stripTrailingZeros().toPlainString()
                            + ", US10Y "
                            + macroContext.us10yYieldValue().stripTrailingZeros().toPlainString()
                            + ", USD/KRW "
                            + macroContext.usdKrwValue().stripTrailingZeros().toPlainString()
                            + ", which can pressure crypto risk appetite.",
                    List.of(
                            "DXY proxy is " + macroContext.dxyProxyValue().stripTrailingZeros().toPlainString() + ".",
                            "US10Y yield is " + macroContext.us10yYieldValue().stripTrailingZeros().toPlainString() + ".",
                            "USD/KRW is " + macroContext.usdKrwValue().stripTrailingZeros().toPlainString() + ".",
                            primaryMacroWindowSummary == null
                                    ? "Macro backdrop is firm."
                                    : primaryMacroWindowSummary.windowType().name()
                                    + " DXY vs average "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentDxyProxyVsAverage())
                                    + ", US10Y vs average "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentUs10yYieldVsAverage())
                                    + ", USD/KRW vs average "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentUsdKrwVsAverage()) + "."
                    )
            ));
        }

        AnalysisOnchainComparisonFact primaryOnchainFact = onchainContext == null
                || onchainContext.comparisonFacts() == null
                || onchainContext.comparisonFacts().isEmpty()
                ? null
                : onchainContext.comparisonFacts().get(0);
        AnalysisOnchainWindowSummary primaryOnchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        boolean anchorContraction = primaryOnchainFact != null
                && primaryOnchainFact.activeAddressChangeRate() != null
                && primaryOnchainFact.transactionCountChangeRate() != null
                && primaryOnchainFact.activeAddressChangeRate().compareTo(new BigDecimal("-0.10")) <= 0
                && primaryOnchainFact.transactionCountChangeRate().compareTo(new BigDecimal("-0.10")) <= 0;
        boolean windowContraction = primaryOnchainWindowSummary != null
                && primaryOnchainWindowSummary.currentActiveAddressVsAverage() != null
                && primaryOnchainWindowSummary.currentTransactionCountVsAverage() != null
                && primaryOnchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                && primaryOnchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0;
        if (anchorContraction || windowContraction) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.ONCHAIN_ACTIVITY_CONTRACTION,
                    "On-chain activity contraction",
                    primaryOnchainFact != null
                            ? primaryOnchainFact.reference().name()
                            + " keeps active addresses "
                            + formattingSupport.signedRatio(primaryOnchainFact.activeAddressChangeRate())
                            + " and transactions "
                            + formattingSupport.signedRatio(primaryOnchainFact.transactionCountChangeRate())
                            + ", which can signal weaker underlying network participation."
                            : primaryOnchainWindowSummary.windowType().name()
                            + " keeps active addresses "
                            + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentActiveAddressVsAverage())
                            + " and transactions "
                            + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentTransactionCountVsAverage())
                            + " versus average, which can signal weaker network participation.",
                    List.of(
                            primaryOnchainFact == null
                                    ? primaryOnchainWindowSummary.windowType().name()
                                    + " active addresses vs average are "
                                    + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentActiveAddressVsAverage()) + "."
                                    : "Active addresses vs " + primaryOnchainFact.reference().name() + " are "
                                    + formattingSupport.signedRatio(primaryOnchainFact.activeAddressChangeRate()) + ".",
                            primaryOnchainFact == null
                                    ? primaryOnchainWindowSummary.windowType().name()
                                    + " transactions vs average are "
                                    + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentTransactionCountVsAverage()) + "."
                                    : "Transactions vs " + primaryOnchainFact.reference().name() + " are "
                                    + formattingSupport.signedRatio(primaryOnchainFact.transactionCountChangeRate()) + "."
                    )
            ));
        }

        if (externalContextComposite != null
                && externalContextComposite.compositeRiskScore() != null
                && externalContextComposite.compositeRiskScore().compareTo(new BigDecimal("1.00")) >= 0
                && externalContextComposite.primarySignalTitle() != null) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.EXTERNAL_RISK_CONFLUENCE,
                    "External risk confluence",
                    "Composite external regime keeps "
                            + externalContextComposite.primarySignalTitle()
                            + " in focus with risk score "
                            + externalContextComposite.compositeRiskScore().stripTrailingZeros().toPlainString()
                            + ".",
                    java.util.stream.Stream.concat(
                                    java.util.stream.Stream.of(
                                            "Dominant external direction is "
                                                    + (externalContextComposite.dominantDirection() == null
                                                    ? "mixed"
                                                    : externalContextComposite.dominantDirection().name().toLowerCase().replace('_', ' '))
                                                    + "."
                                    ),
                                    externalContextComposite.highlights() == null
                                            ? java.util.stream.Stream.empty()
                                            : externalContextComposite.highlights().stream()
                                                                      .limit(2)
                                                                      .map(AnalysisExternalContextHighlight::summary)
                            )
                            .toList()
            ));
        }

        if (candidates.isEmpty()) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.MOMENTUM_TRANSITION,
                    "Momentum transition",
                    "Momentum is not one-sided, so follow-through can slow near key levels.",
                    List.of("Current signals are mixed enough that follow-through may stall near nearby levels.")
            ));
        }

        return candidates;
    }

    List<AnalysisScenario> scenarios(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<String> externalTriggers = externalTriggerConditions(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );
        List<String> externalInvalidations = externalInvalidationSignals(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );
        String externalPath = externalPathSummary(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );

        return switch (trendBias) {
            case BULLISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BULLISH,
                            appendAll(List.of("Price holds above MA20.", "Momentum remains constructive."), externalTriggers),
                            "Price holds above MA20 and extends toward "
                                    + snapshot.getBollingerUpperBand().stripTrailingZeros().toPlainString()
                                    + ". " + externalPath,
                            appendAll(List.of("A loss of MA20 weakens the bullish continuation path."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("Price loses MA20 support."), externalTriggers),
                            "A loss of MA20 can trigger a pullback toward "
                                    + snapshot.getMa60().stripTrailingZeros().toPlainString()
                                    + ". " + externalRiskPathSummary(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                            appendAll(List.of("A fast recovery above MA20 invalidates the pullback case."), externalInvalidations)
                    )
            );
            case BEARISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BEARISH,
                            appendAll(List.of("Price stays below MA20.", "Momentum remains weak."), externalTriggers),
                            "Price stays below MA20 and can probe "
                                    + snapshot.getBollingerLowerBand().stripTrailingZeros().toPlainString()
                                    + ". " + externalRiskPathSummary(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                            appendAll(List.of("A recovery above MA20 weakens the bearish continuation case."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("Price reclaims MA20."), externalTriggers),
                            "A recovery above MA20 can force short-covering toward "
                                    + snapshot.getMa60().stripTrailingZeros().toPlainString()
                                    + ". " + externalPath,
                            appendAll(List.of("Failure back below MA20 invalidates the squeeze scenario."), externalInvalidations)
                    )
            );
            case NEUTRAL -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("Price remains inside the active range.", "Trend strength stays mixed."), externalTriggers),
                            "Price oscillates between support and resistance while waiting for directional confirmation. " + externalPath,
                            appendAll(List.of("A decisive break beyond range extremes invalidates the range case."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Breakout case",
                            AnalysisScenarioBias.DIRECTIONAL,
                            appendAll(List.of("Price breaks beyond the current band extremes."), externalTriggers),
                            "A decisive move beyond the current band extremes can set the next short-term direction. " + externalPath,
                            appendAll(List.of("Failure to hold the breakout level invalidates the directional case."), externalInvalidations)
                    )
            );
        };
    }

    private List<String> externalTriggerConditions(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> triggers = new java.util.ArrayList<>();

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null && derivativeWindowSummary.currentOpenInterestVsAverage() != null
                && derivativeWindowSummary.currentOpenInterestVsAverage().abs().compareTo(new BigDecimal("0.20")) >= 0) {
            triggers.add(derivativeWindowSummary.windowType().name() + " open interest remains "
                    + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                    + " versus average.");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null && (
                macroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                        || macroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
                        || macroWindowSummary.currentUsdKrwVsAverage().compareTo(new BigDecimal("0.01")) >= 0
        )) {
            triggers.add(macroWindowSummary.windowType().name() + " macro backdrop still shows firm dollar/yield pressure.");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null
                && sentimentWindowSummary.currentIndexVsAverage().abs().compareTo(new BigDecimal("0.15")) >= 0) {
            triggers.add(sentimentWindowSummary.windowType().name() + " sentiment remains "
                    + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage())
                    + " versus average.");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null
                && onchainWindowSummary.currentActiveAddressVsAverage() != null
                && onchainWindowSummary.currentTransactionCountVsAverage() != null) {
            if (onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("0.10")) >= 0
                    && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("0.10")) >= 0) {
                triggers.add(onchainWindowSummary.windowType().name() + " on-chain activity remains above average.");
            } else if (onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                    && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0) {
                triggers.add(onchainWindowSummary.windowType().name() + " on-chain activity remains below average.");
            }
        }

        if (externalContextComposite != null && externalContextComposite.highlights() != null) {
            externalContextComposite.highlights().stream()
                                    .limit(2)
                                    .map(AnalysisExternalContextHighlight::summary)
                                    .forEach(triggers::add);
        }

        return triggers;
    }

    private String externalPathSummary(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> clauses = new java.util.ArrayList<>();

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentContext != null && sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null) {
            clauses.add("Sentiment remains " + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage()) + " versus average");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null) {
            clauses.add("macro backdrop keeps DXY " + formattingSupport.signedRatio(macroWindowSummary.currentDxyProxyVsAverage()) + " versus average");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null) {
            clauses.add("on-chain activity sits " + formattingSupport.signedRatio(onchainWindowSummary.currentActiveAddressVsAverage()) + " versus average");
        }

        if (externalContextComposite != null && externalContextComposite.primarySignalTitle() != null) {
            clauses.add("external regime focus stays on " + externalContextComposite.primarySignalTitle());
        }

        return clauses.isEmpty() ? "External context stays mixed." : String.join(", ", clauses) + ".";
    }

    private String externalRiskPathSummary(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> clauses = new java.util.ArrayList<>();

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null && (
                macroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                        || macroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
        )) {
            clauses.add("Firm macro conditions can amplify downside volatility");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null
                && sentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("0.15")) >= 0) {
            clauses.add("elevated greed can unwind quickly");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null
                && onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0) {
            clauses.add("soft on-chain activity can weaken follow-through");
        }

        if (externalContextComposite != null
                && externalContextComposite.compositeRiskScore() != null
                && externalContextComposite.compositeRiskScore().compareTo(new BigDecimal("1.00")) >= 0) {
            clauses.add("external regime confluence keeps risk skew elevated");
        }

        return clauses.isEmpty() ? "External context does not add a strong risk skew." : String.join(", ", clauses) + ".";
    }

    private List<String> externalInvalidationSignals(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> signals = new java.util.ArrayList<>();

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null && derivativeWindowSummary.currentFundingVsAverage() != null) {
            signals.add("Funding and open interest normalize closer to representative averages.");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null) {
            signals.add("Dollar/yield pressure cools back toward window averages.");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null) {
            signals.add("Fear & Greed mean-reverts toward its recent average.");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null) {
            signals.add("On-chain activity stabilizes around its recent average.");
        }

        if (externalContextComposite != null) {
            signals.add("Composite external regime score cools back toward neutral.");
        }

        return signals;
    }

    private List<String> appendAll(List<String> base, List<String> extras) {
        java.util.ArrayList<String> merged = new java.util.ArrayList<>(base);
        merged.addAll(extras);
        return merged;
    }
}

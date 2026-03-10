package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
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

    AnalysisRiskScenarioFactory(
            AnalysisReportFormattingSupport formattingSupport,
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport
    ) {
        this.formattingSupport = formattingSupport;
        this.indicatorStateSupport = indicatorStateSupport;
        this.derivativeContextSupport = derivativeContextSupport;
    }

    List<AnalysisRiskFactor> riskFactors(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisSentimentContext sentimentContext
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

        if (sentimentContext != null && sentimentContext.indexValue().compareTo(new BigDecimal("70")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_GREED_EXTREME,
                    "Sentiment greed extreme",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), so chase risk can rise near resistance.",
                    List.of("Fear & Greed classification is " + sentimentContext.classification() + ".")
            ));
        }

        if (sentimentContext != null && sentimentContext.indexValue().compareTo(new BigDecimal("30")) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_FEAR_EXTREME,
                    "Sentiment fear extreme",
                    "Fear & Greed is at " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + "), so reactive selloffs and whipsaws can expand.",
                    List.of("Fear & Greed classification is " + sentimentContext.classification() + ".")
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

    List<AnalysisScenario> scenarios(MarketIndicatorSnapshotEntity snapshot, AnalysisTrendLabel trendBias) {
        return switch (trendBias) {
            case BULLISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BULLISH,
                            List.of("Price holds above MA20.", "Momentum remains constructive."),
                            "Price holds above MA20 and extends toward " + snapshot.getBollingerUpperBand().stripTrailingZeros().toPlainString() + ".",
                            List.of("A loss of MA20 weakens the bullish continuation path.")
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            List.of("Price loses MA20 support."),
                            "A loss of MA20 can trigger a pullback toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".",
                            List.of("A fast recovery above MA20 invalidates the pullback case.")
                    )
            );
            case BEARISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BEARISH,
                            List.of("Price stays below MA20.", "Momentum remains weak."),
                            "Price stays below MA20 and can probe " + snapshot.getBollingerLowerBand().stripTrailingZeros().toPlainString() + ".",
                            List.of("A recovery above MA20 weakens the bearish continuation case.")
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            List.of("Price reclaims MA20."),
                            "A recovery above MA20 can force short-covering toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".",
                            List.of("Failure back below MA20 invalidates the squeeze scenario.")
                    )
            );
            case NEUTRAL -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.NEUTRAL,
                            List.of("Price remains inside the active range.", "Trend strength stays mixed."),
                            "Price oscillates between support and resistance while waiting for directional confirmation.",
                            List.of("A decisive break beyond range extremes invalidates the range case.")
                    ),
                    new AnalysisScenario(
                            "Breakout case",
                            AnalysisScenarioBias.DIRECTIONAL,
                            List.of("Price breaks beyond the current band extremes."),
                            "A decisive move beyond the current band extremes can set the next short-term direction.",
                            List.of("Failure to hold the breakout level invalidates the directional case.")
                    )
            );
        };
    }
}

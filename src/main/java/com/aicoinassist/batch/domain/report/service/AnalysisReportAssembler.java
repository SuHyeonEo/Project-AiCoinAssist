package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnalysisReportAssembler {

    public AnalysisReportPayload assemble(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        List<AnalysisComparisonHighlight> comparisonHighlights = comparisonHighlights(reportType, comparisonFacts);
        List<AnalysisWindowHighlight> windowHighlights = windowHighlights(reportType, windowSummaries);
        AnalysisDerivativeContext enrichedDerivativeContext = enrichDerivativeContext(reportType, derivativeContext);
        AnalysisTrendLabel trendBias = determineTrendBias(snapshot);
        AnalysisSummaryPayload summary = buildSummary(snapshot, trendBias, reportType, comparisonFacts, comparisonHighlights, windowSummaries, enrichedDerivativeContext, continuityNotes);
        AnalysisMarketContextPayload marketContext = buildMarketContext(snapshot, trendBias, reportType, comparisonFacts, comparisonHighlights, windowHighlights, windowSummaries, enrichedDerivativeContext, continuityNotes);

        return new AnalysisReportPayload(
                summary,
                marketContext,
                comparisonFacts,
                comparisonHighlights,
                windowHighlights,
                continuityNotes,
                windowSummaries,
                enrichedDerivativeContext,
                supportLevels(snapshot),
                resistanceLevels(snapshot),
                riskFactors(snapshot, reportType, enrichedDerivativeContext),
                scenarios(snapshot, trendBias)
        );
    }

    private AnalysisSummaryPayload buildSummary(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        String headline = reportType.name() + " view";
        AnalysisContextHeadlinePayload comparisonHeadline = comparisonContextHeadline(reportType, comparisonFacts, comparisonHighlights);
        AnalysisContextHeadlinePayload windowHeadline = windowContextHeadline(reportType, windowSummaries);
        AnalysisContextHeadlinePayload derivativeHeadline = derivativeContextHeadline(reportType, derivativeContext);
        List<AnalysisContextHeadlinePayload> signalHeadlines = java.util.stream.Stream.of(
                        comparisonHeadline,
                        windowHeadline,
                        derivativeHeadline
                )
                .filter(java.util.Objects::nonNull)
                .toList();

        String keyMessage = snapshot.getSymbol()
                + " is in a "
                + enumLabel(trendBias)
                + " structure with price at "
                + snapshot.getCurrentPrice().stripTrailingZeros().toPlainString()
                + ", RSI14 at "
                + snapshot.getRsi14().stripTrailingZeros().toPlainString()
                + ", and MACD histogram at "
                + snapshot.getMacdHistogram().stripTrailingZeros().toPlainString()
                + ".";

        if (!signalHeadlines.isEmpty()) {
            keyMessage = keyMessage + " " + signalHeadlines.stream()
                                                           .map(AnalysisContextHeadlinePayload::detail)
                                                           .collect(Collectors.joining(" "));
        }

        if (!continuityNotes.isEmpty()) {
            keyMessage = keyMessage + " Continuity: " + continuityNotes.get(0).summary();
        }

        AnalysisOutlookType outlook = switch (trendBias) {
            case BULLISH -> AnalysisOutlookType.CONSTRUCTIVE;
            case BEARISH -> AnalysisOutlookType.DEFENSIVE;
            case NEUTRAL -> AnalysisOutlookType.NEUTRAL;
        };

        AnalysisConfidenceLevel confidence = confidenceLabel(snapshot, comparisonFacts, derivativeContext);

        return new AnalysisSummaryPayload(
                headline,
                outlook,
                confidence,
                keyMessage,
                signalHeadlines
        );
    }

    private AnalysisMarketContextPayload buildMarketContext(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowHighlight> windowHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        String maPositionSummary = comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa20(), "MA20")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa60(), "MA60")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa120(), "MA120");
        String comparisonSummary = comparisonFacts.isEmpty()
                ? "No comparison facts available."
                : comparisonFacts.stream()
                                 .map(this::comparisonFactSummary)
                                 .collect(Collectors.joining("; "));
        List<String> comparisonHighlightDetails = comparisonHighlights.stream()
                                                                     .map(AnalysisComparisonHighlight::detail)
                                                                     .toList();
        AnalysisContextHeadlinePayload comparisonHeadline = comparisonContextHeadline(reportType, comparisonFacts, comparisonHighlights);

        AnalysisWindowSummary primaryWindow = primaryWindow(windowSummaries);
        String windowSummary = null;
        if (primaryWindow != null) {
            windowSummary = "Window summary: "
                    + primaryWindow.windowType().name()
                    + " range "
                    + primaryWindow.low().stripTrailingZeros().toPlainString()
                    + " to "
                    + primaryWindow.high().stripTrailingZeros().toPlainString()
                    + ", position "
                    + percentage(primaryWindow.currentPositionInRange())
                    + ", distance from high "
                    + percentage(primaryWindow.distanceFromWindowHigh())
                    + ", ATR vs average "
                    + signedRatio(primaryWindow.currentAtrVsAverage())
                    + ".";
        }
        List<String> windowHighlightDetails = windowHighlights.stream()
                                                              .map(AnalysisWindowHighlight::detail)
                                                              .toList();
        AnalysisContextHeadlinePayload windowHeadline = windowContextHeadline(reportType, windowSummaries);

        String derivativeContextSummary = null;
        AnalysisContextHeadlinePayload derivativeHeadline = null;
        if (derivativeContext != null) {
            derivativeContextSummary = "Derivative context: open interest "
                    + derivativeContext.openInterest().stripTrailingZeros().toPlainString()
                    + ", funding "
                    + fundingRatePercentage(derivativeContext.lastFundingRate())
                    + ", basis "
                    + signedPercent(derivativeContext.markIndexBasisRate())
                    + ", mark-index spread "
                    + derivativeContext.markPrice()
                                       .subtract(derivativeContext.indexPrice())
                                       .stripTrailingZeros()
                                       .toPlainString()
                    + ", next funding in "
                    + hoursUntilNextFunding(null, derivativeContext)
                    + "h.";

            AnalysisDerivativeWindowSummary derivativeWindowSummary = primaryDerivativeWindowSummary(reportType, derivativeContext);
            if (derivativeWindowSummary != null) {
                derivativeContextSummary = derivativeContextSummary + " Derivative window summary: "
                        + derivativeWindowSummary.windowType().name()
                        + " OI vs average "
                        + signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                        + ", funding vs average "
                        + signedRatio(derivativeWindowSummary.currentFundingVsAverage())
                        + ", basis vs average "
                        + signedRatio(derivativeWindowSummary.currentBasisVsAverage())
                        + ".";
            }
            if (derivativeContext.highlights() != null && !derivativeContext.highlights().isEmpty()) {
                derivativeContextSummary = derivativeContextSummary + " Derivative highlights: "
                        + derivativeContext.highlights().stream()
                                           .map(AnalysisDerivativeHighlight::summary)
                                           .collect(Collectors.joining(" "));
            }
            derivativeHeadline = derivativeContextHeadline(reportType, derivativeContext);
        }

        AnalysisContinuityContextPayload continuityContext = continuityNotes.isEmpty()
                ? null
                : new AnalysisContinuityContextPayload(
                        continuityNotes.get(0).reference(),
                        continuityNotes.get(0).summary()
                );

        return new AnalysisMarketContextPayload(
                new AnalysisCurrentStatePayload(
                        snapshot.getCurrentPrice(),
                        trendBias,
                        volatilityLabel(snapshot),
                        rangePositionLabel(primaryWindow),
                        maPositionSummary,
                        momentumSummary(snapshot)
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
                derivativeContextSummary,
                derivativeHeadline,
                continuityContext
        );
    }

    private List<AnalysisPriceLevel> supportLevels(MarketIndicatorSnapshotEntity snapshot) {
        List<AnalysisPriceLevel> candidates = List.of(
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA20, snapshot.getMa20(), "Short-term average support"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average support"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA120, snapshot.getMa120(), "Longer trend average support"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.BB_LOWER, snapshot.getBollingerLowerBand(), "Lower Bollinger band support")
        );

        return candidates.stream()
                         .filter(level -> level.price().compareTo(snapshot.getCurrentPrice()) <= 0)
                         .sorted(Comparator.comparing(AnalysisPriceLevel::price).reversed())
                         .limit(2)
                         .toList();
    }

    private List<AnalysisPriceLevel> resistanceLevels(MarketIndicatorSnapshotEntity snapshot) {
        List<AnalysisPriceLevel> candidates = List.of(
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA20, snapshot.getMa20(), "Short-term average resistance"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average resistance"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.MA120, snapshot.getMa120(), "Longer trend average resistance"),
                new AnalysisPriceLevel(AnalysisPriceLevelLabel.BB_UPPER, snapshot.getBollingerUpperBand(), "Upper Bollinger band resistance")
        );

        List<AnalysisPriceLevel> levels = candidates.stream()
                                                    .filter(level -> level.price().compareTo(snapshot.getCurrentPrice()) >= 0)
                                                    .sorted(Comparator.comparing(AnalysisPriceLevel::price))
                                                    .limit(2)
                                                    .toList();

        if (!levels.isEmpty()) {
            return levels;
        }

        return candidates.stream()
                         .sorted(Comparator.comparing(AnalysisPriceLevel::price).reversed())
                         .limit(2)
                         .toList();
    }

    private List<AnalysisRiskFactor> riskFactors(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        List<AnalysisRiskFactor> candidates = new java.util.ArrayList<AnalysisRiskFactor>();

        if (snapshot.getRsi14().compareTo(new BigDecimal("70")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_OVERHEATING,
                    "RSI overheating",
                    "RSI14 is above 70, so upside continuation can weaken quickly."
            ));
        }

        if (snapshot.getRsi14().compareTo(new BigDecimal("30")) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_COMPRESSION,
                    "RSI compression",
                    "RSI14 is below 30, so downside can be stretched and whipsaws can increase."
            ));
        }

        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerUpperBand()) >= 0
                || snapshot.getCurrentPrice().compareTo(snapshot.getBollingerLowerBand()) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BAND_EXTENSION,
                    "Band extension",
                    "Price is trading at an outer Bollinger band, which raises reversion risk."
            ));
        }

        if (atrRatio(snapshot).compareTo(new BigDecimal("3.00")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.ELEVATED_VOLATILITY,
                    "Elevated volatility",
                    "ATR14 is more than 3% of price, so intraperiod swings can expand."
            ));
        }

        if (derivativeContext != null && derivativeContext.lastFundingRate().abs().compareTo(new BigDecimal("0.0004")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.FUNDING_SKEW,
                    "Funding skew",
                    "Funding is running at " + fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", which can signal crowded directional leverage."
            ));
        }

        if (derivativeContext != null && derivativeContext.markIndexBasisRate().abs().compareTo(new BigDecimal("0.05")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BASIS_EXPANSION,
                    "Basis expansion",
                    "Mark/index basis is " + signedPercent(derivativeContext.markIndexBasisRate())
                            + ", so futures positioning is trading away from spot."
            ));
        }

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null
                && derivativeWindowSummary.currentOpenInterestVsAverage() != null
                && derivativeWindowSummary.currentOpenInterestVsAverage().abs().compareTo(new BigDecimal("0.20")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.OPEN_INTEREST_CROWDING,
                    "Open interest crowding",
                    "Open interest is running " + signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                            + " versus the representative window average."
            ));
        }

        if (candidates.isEmpty()) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.MOMENTUM_TRANSITION,
                    "Momentum transition",
                    "Momentum is not one-sided, so follow-through can slow near key levels."
            ));
        }

        return candidates;
    }

    private List<AnalysisScenario> scenarios(MarketIndicatorSnapshotEntity snapshot, AnalysisTrendLabel trendBias) {
        return switch (trendBias) {
            case BULLISH -> List.of(
                    new AnalysisScenario("Base case", AnalysisScenarioBias.BULLISH, "Price holds above MA20 and extends toward " + snapshot.getBollingerUpperBand().stripTrailingZeros().toPlainString() + "."),
                    new AnalysisScenario("Risk case", AnalysisScenarioBias.NEUTRAL, "A loss of MA20 can trigger a pullback toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".")
            );
            case BEARISH -> List.of(
                    new AnalysisScenario("Base case", AnalysisScenarioBias.BEARISH, "Price stays below MA20 and can probe " + snapshot.getBollingerLowerBand().stripTrailingZeros().toPlainString() + "."),
                    new AnalysisScenario("Risk case", AnalysisScenarioBias.NEUTRAL, "A recovery above MA20 can force short-covering toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".")
            );
            case NEUTRAL -> List.of(
                    new AnalysisScenario("Base case", AnalysisScenarioBias.NEUTRAL, "Price oscillates between support and resistance while waiting for directional confirmation."),
                    new AnalysisScenario("Breakout case", AnalysisScenarioBias.DIRECTIONAL, "A decisive move beyond the current band extremes can set the next short-term direction.")
            );
        };
    }

    private AnalysisTrendLabel determineTrendBias(MarketIndicatorSnapshotEntity snapshot) {
        boolean bullishAlignment = snapshot.getCurrentPrice().compareTo(snapshot.getMa20()) >= 0
                && snapshot.getMa20().compareTo(snapshot.getMa60()) >= 0
                && snapshot.getMacdHistogram().compareTo(BigDecimal.ZERO) >= 0;

        boolean bearishAlignment = snapshot.getCurrentPrice().compareTo(snapshot.getMa20()) <= 0
                && snapshot.getMa20().compareTo(snapshot.getMa60()) <= 0
                && snapshot.getMacdHistogram().compareTo(BigDecimal.ZERO) <= 0;

        if (bullishAlignment) {
            return AnalysisTrendLabel.BULLISH;
        }

        if (bearishAlignment) {
            return AnalysisTrendLabel.BEARISH;
        }

        return AnalysisTrendLabel.NEUTRAL;
    }

    private String comparePriceToMovingAverage(BigDecimal currentPrice, BigDecimal movingAverage, String label) {
        if (currentPrice.compareTo(movingAverage) >= 0) {
            return "above " + label;
        }

        return "below " + label;
    }

    private String describeBandPosition(MarketIndicatorSnapshotEntity snapshot) {
        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerUpperBand()) >= 0) {
            return "at or above the upper Bollinger band";
        }

        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerLowerBand()) <= 0) {
            return "at or below the lower Bollinger band";
        }

        return "inside the Bollinger band range";
    }

    private AnalysisVolatilityLabel volatilityLabel(MarketIndicatorSnapshotEntity snapshot) {
        BigDecimal atrPercent = atrRatio(snapshot);
        if (atrPercent.compareTo(new BigDecimal("3.00")) >= 0) {
            return AnalysisVolatilityLabel.ELEVATED;
        }
        if (atrPercent.compareTo(new BigDecimal("1.50")) >= 0) {
            return AnalysisVolatilityLabel.MODERATE;
        }
        return AnalysisVolatilityLabel.CONTAINED;
    }

    private AnalysisRangePositionLabel rangePositionLabel(AnalysisWindowSummary primaryWindow) {
        if (primaryWindow == null || primaryWindow.currentPositionInRange() == null) {
            return AnalysisRangePositionLabel.UNAVAILABLE;
        }

        BigDecimal position = primaryWindow.currentPositionInRange();
        if (position.compareTo(new BigDecimal("0.67")) >= 0) {
            return AnalysisRangePositionLabel.UPPER_RANGE;
        }
        if (position.compareTo(new BigDecimal("0.33")) <= 0) {
            return AnalysisRangePositionLabel.LOWER_RANGE;
        }
        return AnalysisRangePositionLabel.MID_RANGE;
    }

    private String momentumSummary(MarketIndicatorSnapshotEntity snapshot) {
        return "RSI14 "
                + snapshot.getRsi14().stripTrailingZeros().toPlainString()
                + ", MACD histogram "
                + snapshot.getMacdHistogram().stripTrailingZeros().toPlainString();
    }

    private AnalysisConfidenceLevel confidenceLabel(
            MarketIndicatorSnapshotEntity snapshot,
            List<AnalysisComparisonFact> comparisonFacts,
            AnalysisDerivativeContext derivativeContext
    ) {
        boolean directionalTrend = determineTrendBias(snapshot) != AnalysisTrendLabel.NEUTRAL;
        boolean hasComparisons = comparisonFacts != null && !comparisonFacts.isEmpty();
        boolean hasDerivativeHighlights = derivativeContext != null
                && derivativeContext.highlights() != null
                && !derivativeContext.highlights().isEmpty();

        if (directionalTrend && hasComparisons && hasDerivativeHighlights) {
            return AnalysisConfidenceLevel.HIGH;
        }
        if (directionalTrend && hasComparisons) {
            return AnalysisConfidenceLevel.MEDIUM;
        }
        return AnalysisConfidenceLevel.LOW;
    }

    private String enumLabel(Enum<?> value) {
        return value.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    }

    private BigDecimal atrRatio(MarketIndicatorSnapshotEntity snapshot) {
        return snapshot.getAtr14()
                       .multiply(new BigDecimal("100"))
                       .divide(snapshot.getCurrentPrice(), 2, RoundingMode.HALF_UP);
    }

    private String signed(BigDecimal value) {
        String plainValue = value.stripTrailingZeros().toPlainString();
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + plainValue;
        }
        return plainValue;
    }

    private List<AnalysisComparisonHighlight> comparisonHighlights(
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

    private List<AnalysisWindowHighlight> windowHighlights(
            AnalysisReportType reportType,
            List<AnalysisWindowSummary> windowSummaries
    ) {
        Map<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType, AnalysisWindowSummary> summaryByType = windowSummaries.stream()
                                                                                                                           .collect(Collectors.toMap(
                                                                                                                                   AnalysisWindowSummary::windowType,
                                                                                                                                   summary -> summary,
                                                                                                                                   (left, right) -> left
                                                                                                                           ));

        return (switch (reportType) {
            case SHORT_TERM -> List.of(
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_1D),
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_7D)
            );
            case MID_TERM -> List.of(
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_7D),
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D)
            );
            case LONG_TERM -> List.of(
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_180D),
                    summaryByType.get(com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_52W)
            );
        }).stream()
         .filter(java.util.Objects::nonNull)
         .map(this::toWindowHighlight)
         .toList();
    }

    private AnalysisComparisonHighlight toHighlight(AnalysisComparisonFact fact) {
        return switch (fact.reference()) {
            case PREV_BATCH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Since the previous batch, price changed " + signed(fact.priceChangeRate()) + "% and RSI14 moved " + signed(fact.rsiDelta()) + ".",
                    "PREV_BATCH confirms the latest impulse with MACD histogram Δ " + signed(fact.macdHistogramDelta()) + "."
            );
            case D1, D3, D7, D14, D30, D90, D180 -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    fact.reference().name() + " shows price " + signed(fact.priceChangeRate()) + "% versus the reference point.",
                    fact.reference().name() + " keeps RSI Δ " + signed(fact.rsiDelta()) + " and MACD hist Δ " + signed(fact.macdHistogramDelta()) + "."
            );
            case PREV_SHORT_REPORT, PREV_MID_REPORT, PREV_LONG_REPORT -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Versus " + fact.reference().name() + ", price changed " + signed(fact.priceChangeRate()) + "%.",
                    fact.reference().name() + " comparison shows RSI Δ " + signed(fact.rsiDelta()) + " and ATR change " + signed(fact.atrChangeRate()) + "%."
            );
            case Y52_HIGH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Price is " + distanceFromExtremum(fact.priceChangeRate(), "below") + " the 52-week high.",
                    "Y52_HIGH keeps long-term upside distance at " + signed(fact.priceChangeRate()) + "% from the cycle peak."
            );
            case Y52_LOW -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "Price is " + distanceFromExtremum(fact.priceChangeRate(), "above") + " the 52-week low.",
                    "Y52_LOW shows the market remains " + signed(fact.priceChangeRate()) + "% above the cycle floor."
            );
        };
    }

    private String comparisonFactSummary(AnalysisComparisonFact fact) {
        return fact.reference().name()
                + " price "
                + signed(fact.priceChangeRate())
                + "%, RSI Δ "
                + signed(fact.rsiDelta())
                + ", MACD hist Δ "
                + signed(fact.macdHistogramDelta());
    }

    private AnalysisWindowHighlight toWindowHighlight(AnalysisWindowSummary summary) {
        return new AnalysisWindowHighlight(
                summary.windowType(),
                summary.windowType().name() + " keeps price at " + percentage(summary.currentPositionInRange()) + " of the range.",
                summary.windowType().name()
                        + " volume vs average "
                        + signedRatio(summary.currentVolumeVsAverage())
                        + ", ATR vs average "
                        + signedRatio(summary.currentAtrVsAverage())
                        + ", distance from range high "
                        + percentage(summary.distanceFromWindowHigh())
                        + "."
        );
    }

    private String distanceFromExtremum(BigDecimal priceChangeRate, String relation) {
        BigDecimal absoluteValue = priceChangeRate.abs();
        return absoluteValue.stripTrailingZeros().toPlainString() + "% " + relation;
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

    private AnalysisDerivativeWindowSummary primaryDerivativeWindowSummary(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext
    ) {
        if (derivativeContext.windowSummaries() == null || derivativeContext.windowSummaries().isEmpty()) {
            return null;
        }

        Map<com.aicoinassist.batch.domain.market.enumtype.MarketWindowType, AnalysisDerivativeWindowSummary> summaryByType = derivativeContext.windowSummaries()
                                                                                                                                 .stream()
                                                                                                                                 .collect(Collectors.toMap(
                                                                                                                                         AnalysisDerivativeWindowSummary::windowType,
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
                       .orElse(derivativeContext.windowSummaries().get(derivativeContext.windowSummaries().size() - 1));
    }

    private AnalysisDerivativeContext enrichDerivativeContext(
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
                            + signedRatio(primaryComparisonFact.openInterestChangeRate())
                            + ", funding Δ "
                            + fundingRatePercentage(primaryComparisonFact.fundingRateDelta())
                            + ", basis Δ "
                            + signedPercent(primaryComparisonFact.basisRateDelta()),
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
                            + signedRatio(primaryWindowSummary.currentFundingVsAverage())
                            + ", OI vs average "
                            + signedRatio(primaryWindowSummary.currentOpenInterestVsAverage())
                            + ", basis vs average "
                            + signedRatio(primaryWindowSummary.currentBasisVsAverage()),
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
                    "Current funding is " + fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", which points to leveraged directional crowding.",
                    AnalysisDerivativeHighlightImportance.HIGH,
                    AnalysisDerivativeMetricType.FUNDING_RATE,
                    null,
                    null
            ));
        }

        return highlights.stream().limit(3).toList();
    }

    private AnalysisContextHeadlinePayload comparisonContextHeadline(
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

    private AnalysisContextHeadlinePayload windowContextHeadline(
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
                        + percentage(primaryWindow.currentPositionInRange())
                        + " of the range with volume "
                        + signedRatio(primaryWindow.currentVolumeVsAverage())
                        + " versus average.",
                reportType == AnalysisReportType.LONG_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    private AnalysisContextHeadlinePayload derivativeContextHeadline(
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
                        + signedRatio(primaryDerivativeFact.openInterestChangeRate())
                        + " with funding Δ "
                        + fundingRatePercentage(primaryDerivativeFact.fundingRateDelta())
                        + ".",
                reportType == AnalysisReportType.SHORT_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
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

    private AnalysisWindowSummary primaryWindow(List<AnalysisWindowSummary> windowSummaries) {
        if (windowSummaries == null || windowSummaries.isEmpty()) {
            return null;
        }
        return windowSummaries.get(windowSummaries.size() - 1);
    }

    private String percentage(BigDecimal value) {
        if (value == null) {
            return "unavailable";
        }

        return value.multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString() + "%";
    }

    private String signedRatio(BigDecimal value) {
        if (value == null) {
            return "unavailable";
        }

        BigDecimal asPercent = value.multiply(new BigDecimal("100"))
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .stripTrailingZeros();
        if (asPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + asPercent.toPlainString() + "%";
        }
        return asPercent.toPlainString() + "%";
    }

    private String fundingRatePercentage(BigDecimal fundingRate) {
        if (fundingRate == null) {
            return "unavailable";
        }

        BigDecimal percentage = fundingRate.multiply(new BigDecimal("100"))
                                           .setScale(4, RoundingMode.HALF_UP)
                                           .stripTrailingZeros();
        if (percentage.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + percentage.toPlainString() + "%";
        }
        return percentage.toPlainString() + "%";
    }

    private String signedPercent(BigDecimal percentValue) {
        if (percentValue == null) {
            return "unavailable";
        }

        BigDecimal normalized = percentValue.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        if (normalized.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + normalized.toPlainString() + "%";
        }
        return normalized.toPlainString() + "%";
    }

    private long hoursUntilNextFunding(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisDerivativeContext derivativeContext
    ) {
        java.time.Instant baseTime = snapshot == null
                ? derivativeContext.snapshotTime()
                : snapshot.getSnapshotTime();

        long durationSeconds = java.time.Duration.between(baseTime, derivativeContext.nextFundingTime()).toSeconds();
        if (durationSeconds <= 0) {
            return 0;
        }

        return java.time.Duration.ofSeconds(durationSeconds).toHours();
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
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
        String trendBias = determineTrendBias(snapshot);
        String summary = buildSummary(snapshot, trendBias, reportType, comparisonFacts, comparisonHighlights, windowSummaries, enrichedDerivativeContext, continuityNotes);
        String marketContext = buildMarketContext(snapshot, trendBias, reportType, comparisonFacts, comparisonHighlights, windowHighlights, windowSummaries, enrichedDerivativeContext, continuityNotes);

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

    private String buildSummary(
            MarketIndicatorSnapshotEntity snapshot,
            String trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        String summary = reportType.name() + " view: "
                + snapshot.getSymbol()
                + " is in a "
                + trendBias
                + " structure with price at "
                + snapshot.getCurrentPrice().stripTrailingZeros().toPlainString()
                + ", RSI14 at "
                + snapshot.getRsi14().stripTrailingZeros().toPlainString()
                + ", and MACD histogram at "
                + snapshot.getMacdHistogram().stripTrailingZeros().toPlainString()
                + ".";

        if (comparisonFacts.isEmpty()) {
            return summary;
        }

        if (!comparisonHighlights.isEmpty()) {
            summary = summary + " " + comparisonHighlights.get(0).headline();
        } else if (!comparisonFacts.isEmpty()) {
            AnalysisComparisonFact primaryFact = comparisonFacts.get(0);
            summary = summary + " Versus "
                    + primaryFact.reference().name()
                    + ", price changed "
                    + signed(primaryFact.priceChangeRate())
                    + "% and RSI14 moved "
                    + signed(primaryFact.rsiDelta())
                    + ".";
        }

        AnalysisWindowSummary primaryWindow = primaryWindow(windowSummaries);
        if (primaryWindow != null) {
            summary = summary + " "
                    + primaryWindow.windowType().name()
                    + " keeps price at "
                    + percentage(primaryWindow.currentPositionInRange())
                    + " of the window range with volume "
                    + signedRatio(primaryWindow.currentVolumeVsAverage())
                    + " versus the window average.";
        }

        if (derivativeContext != null) {
            summary = summary + " Derivatives show funding "
                    + fundingRatePercentage(derivativeContext.lastFundingRate())
                    + " with basis "
                    + signedPercent(derivativeContext.markIndexBasisRate())
                    + " and next funding in "
                    + hoursUntilNextFunding(snapshot, derivativeContext)
                    + "h.";
            AnalysisDerivativeComparisonFact primaryDerivativeFact = primaryDerivativeFact(reportType, derivativeContext);
            if (primaryDerivativeFact != null) {
                summary = summary + " "
                        + primaryDerivativeFact.reference().name()
                        + " keeps OI "
                        + signedRatio(primaryDerivativeFact.openInterestChangeRate())
                        + " with funding Δ "
                        + fundingRatePercentage(primaryDerivativeFact.fundingRateDelta())
                        + ".";
            }
            if (derivativeContext.highlights() != null && !derivativeContext.highlights().isEmpty()) {
                summary = summary + " " + derivativeContext.highlights().get(0).summary();
            }
        }

        if (continuityNotes.isEmpty()) {
            return summary;
        }

        return summary + " Continuity: " + continuityNotes.get(0).summary();
    }

    private String buildMarketContext(
            MarketIndicatorSnapshotEntity snapshot,
            String trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowHighlight> windowHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        String maContext = comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa20(), "MA20")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa60(), "MA60")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa120(), "MA120");

        String context = "Trend bias is "
                + trendBias
                + ". Price is "
                + describeBandPosition(snapshot)
                + " and ATR14 is "
                + atrRatio(snapshot).stripTrailingZeros().toPlainString()
                + "% of current price. "
                + maContext
                + ".";

        if (comparisonFacts.isEmpty()) {
            return appendWindowSummaryContext(context, reportType, windowHighlights, windowSummaries, derivativeContext, continuityNotes);
        }

        String highlightsText = comparisonHighlights.isEmpty()
                ? ""
                : " Highlights: " + comparisonHighlights.stream()
                                                        .map(AnalysisComparisonHighlight::detail)
                                                        .collect(Collectors.joining(" "));

        return appendWindowSummaryContext(context + " Comparison facts: "
                + comparisonFacts.stream()
                                 .map(this::comparisonFactSummary)
                                 .collect(Collectors.joining("; "))
                + "."
                + highlightsText, reportType, windowHighlights, windowSummaries, derivativeContext, continuityNotes);
    }

    private String appendWindowSummaryContext(
            String context,
            AnalysisReportType reportType,
            List<AnalysisWindowHighlight> windowHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            List<AnalysisContinuityNote> continuityNotes
    ) {
        AnalysisWindowSummary primaryWindow = primaryWindow(windowSummaries);
        if (primaryWindow != null) {
            context = context + " Window summary: "
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

        if (!windowHighlights.isEmpty()) {
            context = context + " Window highlights: "
                    + windowHighlights.stream()
                                      .map(AnalysisWindowHighlight::detail)
                                      .collect(Collectors.joining(" "));
        }

        if (derivativeContext != null) {
            context = context + " Derivative context: open interest "
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
                context = context + " Derivative window summary: "
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
                context = context + " Derivative highlights: "
                        + derivativeContext.highlights().stream()
                                           .map(AnalysisDerivativeHighlight::summary)
                                           .collect(Collectors.joining(" "));
            }
        }

        if (continuityNotes.isEmpty()) {
            return context;
        }

        return context + " Continuity note: " + continuityNotes.get(0).summary();
    }

    private List<AnalysisPriceLevel> supportLevels(MarketIndicatorSnapshotEntity snapshot) {
        List<AnalysisPriceLevel> candidates = List.of(
                new AnalysisPriceLevel("MA20", snapshot.getMa20(), "Short-term average support"),
                new AnalysisPriceLevel("MA60", snapshot.getMa60(), "Mid-trend average support"),
                new AnalysisPriceLevel("MA120", snapshot.getMa120(), "Longer trend average support"),
                new AnalysisPriceLevel("BB_LOWER", snapshot.getBollingerLowerBand(), "Lower Bollinger band support")
        );

        return candidates.stream()
                         .filter(level -> level.price().compareTo(snapshot.getCurrentPrice()) <= 0)
                         .sorted(Comparator.comparing(AnalysisPriceLevel::price).reversed())
                         .limit(2)
                         .toList();
    }

    private List<AnalysisPriceLevel> resistanceLevels(MarketIndicatorSnapshotEntity snapshot) {
        List<AnalysisPriceLevel> candidates = List.of(
                new AnalysisPriceLevel("MA20", snapshot.getMa20(), "Short-term average resistance"),
                new AnalysisPriceLevel("MA60", snapshot.getMa60(), "Mid-trend average resistance"),
                new AnalysisPriceLevel("MA120", snapshot.getMa120(), "Longer trend average resistance"),
                new AnalysisPriceLevel("BB_UPPER", snapshot.getBollingerUpperBand(), "Upper Bollinger band resistance")
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
            candidates.add(new AnalysisRiskFactor("RSI overheating", "RSI14 is above 70, so upside continuation can weaken quickly."));
        }

        if (snapshot.getRsi14().compareTo(new BigDecimal("30")) <= 0) {
            candidates.add(new AnalysisRiskFactor("RSI compression", "RSI14 is below 30, so downside can be stretched and whipsaws can increase."));
        }

        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerUpperBand()) >= 0
                || snapshot.getCurrentPrice().compareTo(snapshot.getBollingerLowerBand()) <= 0) {
            candidates.add(new AnalysisRiskFactor("Band extension", "Price is trading at an outer Bollinger band, which raises reversion risk."));
        }

        if (atrRatio(snapshot).compareTo(new BigDecimal("3.00")) >= 0) {
            candidates.add(new AnalysisRiskFactor("Elevated volatility", "ATR14 is more than 3% of price, so intraperiod swings can expand."));
        }

        if (derivativeContext != null && derivativeContext.lastFundingRate().abs().compareTo(new BigDecimal("0.0004")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    "Funding skew",
                    "Funding is running at " + fundingRatePercentage(derivativeContext.lastFundingRate())
                            + ", which can signal crowded directional leverage."
            ));
        }

        if (derivativeContext != null && derivativeContext.markIndexBasisRate().abs().compareTo(new BigDecimal("0.05")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
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
                    "Open interest crowding",
                    "Open interest is running " + signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                            + " versus the representative window average."
            ));
        }

        if (candidates.isEmpty()) {
            candidates.add(new AnalysisRiskFactor("Momentum transition", "Momentum is not one-sided, so follow-through can slow near key levels."));
        }

        return candidates;
    }

    private List<AnalysisScenario> scenarios(MarketIndicatorSnapshotEntity snapshot, String trendBias) {
        return switch (trendBias) {
            case "bullish" -> List.of(
                    new AnalysisScenario("Base case", "bullish", "Price holds above MA20 and extends toward " + snapshot.getBollingerUpperBand().stripTrailingZeros().toPlainString() + "."),
                    new AnalysisScenario("Risk case", "neutral", "A loss of MA20 can trigger a pullback toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".")
            );
            case "bearish" -> List.of(
                    new AnalysisScenario("Base case", "bearish", "Price stays below MA20 and can probe " + snapshot.getBollingerLowerBand().stripTrailingZeros().toPlainString() + "."),
                    new AnalysisScenario("Risk case", "neutral", "A recovery above MA20 can force short-covering toward " + snapshot.getMa60().stripTrailingZeros().toPlainString() + ".")
            );
            default -> List.of(
                    new AnalysisScenario("Base case", "neutral", "Price oscillates between support and resistance while waiting for directional confirmation."),
                    new AnalysisScenario("Breakout case", "directional", "A decisive move beyond the current band extremes can set the next short-term direction.")
            );
        };
    }

    private String determineTrendBias(MarketIndicatorSnapshotEntity snapshot) {
        boolean bullishAlignment = snapshot.getCurrentPrice().compareTo(snapshot.getMa20()) >= 0
                && snapshot.getMa20().compareTo(snapshot.getMa60()) >= 0
                && snapshot.getMacdHistogram().compareTo(BigDecimal.ZERO) >= 0;

        boolean bearishAlignment = snapshot.getCurrentPrice().compareTo(snapshot.getMa20()) <= 0
                && snapshot.getMa20().compareTo(snapshot.getMa60()) <= 0
                && snapshot.getMacdHistogram().compareTo(BigDecimal.ZERO) <= 0;

        if (bullishAlignment) {
            return "bullish";
        }

        if (bearishAlignment) {
            return "bearish";
        }

        return "neutral";
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
                    reportType == AnalysisReportType.SHORT_TERM ? "high" : "medium",
                    "openInterest",
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
                    reportType == AnalysisReportType.LONG_TERM ? "high" : "medium",
                    "fundingRate",
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
                    "high",
                    "fundingRate",
                    null,
                    null
            ));
        }

        return highlights.stream().limit(3).toList();
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

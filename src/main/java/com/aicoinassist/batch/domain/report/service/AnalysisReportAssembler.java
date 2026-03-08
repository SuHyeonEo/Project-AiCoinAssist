package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Component
public class AnalysisReportAssembler {

    public AnalysisReportPayload assemble(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType
    ) {
        String trendBias = determineTrendBias(snapshot);
        String summary = buildSummary(snapshot, trendBias, reportType);
        String marketContext = buildMarketContext(snapshot, trendBias);

        return new AnalysisReportPayload(
                summary,
                marketContext,
                supportLevels(snapshot),
                resistanceLevels(snapshot),
                riskFactors(snapshot),
                scenarios(snapshot, trendBias)
        );
    }

    private String buildSummary(MarketIndicatorSnapshotEntity snapshot, String trendBias, AnalysisReportType reportType) {
        return reportType.name() + " view: "
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
    }

    private String buildMarketContext(MarketIndicatorSnapshotEntity snapshot, String trendBias) {
        String maContext = comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa20(), "MA20")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa60(), "MA60")
                + ", "
                + comparePriceToMovingAverage(snapshot.getCurrentPrice(), snapshot.getMa120(), "MA120");

        return "Trend bias is "
                + trendBias
                + ". Price is "
                + describeBandPosition(snapshot)
                + " and ATR14 is "
                + atrRatio(snapshot).stripTrailingZeros().toPlainString()
                + "% of current price. "
                + maContext
                + ".";
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

    private List<AnalysisRiskFactor> riskFactors(MarketIndicatorSnapshotEntity snapshot) {
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
}

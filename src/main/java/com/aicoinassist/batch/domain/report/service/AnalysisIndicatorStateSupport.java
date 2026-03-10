package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class AnalysisIndicatorStateSupport {

    AnalysisTrendLabel determineTrendBias(MarketIndicatorSnapshotEntity snapshot) {
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

    AnalysisVolatilityLabel volatilityLabel(MarketIndicatorSnapshotEntity snapshot) {
        BigDecimal atrPercent = atrRatio(snapshot);
        if (atrPercent.compareTo(new BigDecimal("3.00")) >= 0) {
            return AnalysisVolatilityLabel.ELEVATED;
        }
        if (atrPercent.compareTo(new BigDecimal("1.50")) >= 0) {
            return AnalysisVolatilityLabel.MODERATE;
        }
        return AnalysisVolatilityLabel.CONTAINED;
    }

    AnalysisRangePositionLabel rangePositionLabel(AnalysisWindowSummary primaryWindow) {
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

    AnalysisMomentumStatePayload momentumState(MarketIndicatorSnapshotEntity snapshot) {
        return new AnalysisMomentumStatePayload(
                snapshot.getRsi14(),
                snapshot.getMacdHistogram(),
                "RSI14 "
                        + snapshot.getRsi14().stripTrailingZeros().toPlainString()
                        + ", MACD histogram "
                        + snapshot.getMacdHistogram().stripTrailingZeros().toPlainString()
        );
    }

    AnalysisMovingAveragePositionPayload movingAveragePosition(
            BigDecimal currentPrice,
            BigDecimal movingAverage,
            String movingAverageName
    ) {
        return new AnalysisMovingAveragePositionPayload(
                movingAverageName,
                movingAverage,
                currentPrice.compareTo(movingAverage) >= 0
        );
    }

    AnalysisConfidenceLevel confidenceLabel(
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

    BigDecimal atrRatio(MarketIndicatorSnapshotEntity snapshot) {
        return snapshot.getAtr14()
                       .multiply(new BigDecimal("100"))
                       .divide(snapshot.getCurrentPrice(), 2, RoundingMode.HALF_UP);
    }
}

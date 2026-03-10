package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MarketCandidateLevelSnapshotService {

    private static final int SCORE_SCALE = 8;

    public List<MarketCandidateLevelSnapshot> createAll(MarketIndicatorSnapshotEntity snapshot) {
        List<MarketCandidateLevelSnapshot> candidates = List.of(
                candidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA20, snapshot.getMa20(), "Short-term average support"),
                candidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average support"),
                candidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA120, snapshot.getMa120(), "Longer trend average support"),
                candidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.BB_LOWER, snapshot.getBollingerLowerBand(), "Lower Bollinger band support"),
                candidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA20, snapshot.getMa20(), "Short-term average resistance"),
                candidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average resistance"),
                candidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA120, snapshot.getMa120(), "Longer trend average resistance"),
                candidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.BB_UPPER, snapshot.getBollingerUpperBand(), "Upper Bollinger band resistance")
        );

        List<MarketCandidateLevelSnapshot> results = new ArrayList<>();
        results.addAll(selectSupportLevels(snapshot, candidates));
        results.addAll(selectResistanceLevels(snapshot, candidates));
        return results;
    }

    private List<MarketCandidateLevelSnapshot> selectSupportLevels(
            MarketIndicatorSnapshotEntity snapshot,
            List<MarketCandidateLevelSnapshot> candidates
    ) {
        return candidates.stream()
                         .filter(candidate -> candidate.levelType() == MarketCandidateLevelType.SUPPORT)
                         .filter(candidate -> candidate.levelPrice().compareTo(snapshot.getCurrentPrice()) <= 0)
                         .sorted(Comparator.comparing(MarketCandidateLevelSnapshot::levelPrice).reversed())
                         .limit(2)
                         .toList();
    }

    private List<MarketCandidateLevelSnapshot> selectResistanceLevels(
            MarketIndicatorSnapshotEntity snapshot,
            List<MarketCandidateLevelSnapshot> candidates
    ) {
        List<MarketCandidateLevelSnapshot> levels = candidates.stream()
                                                              .filter(candidate -> candidate.levelType() == MarketCandidateLevelType.RESISTANCE)
                                                              .filter(candidate -> candidate.levelPrice().compareTo(snapshot.getCurrentPrice()) >= 0)
                                                              .sorted(Comparator.comparing(MarketCandidateLevelSnapshot::levelPrice))
                                                              .limit(2)
                                                              .toList();
        if (!levels.isEmpty()) {
            return levels;
        }

        return candidates.stream()
                         .filter(candidate -> candidate.levelType() == MarketCandidateLevelType.RESISTANCE)
                         .sorted(Comparator.comparing(MarketCandidateLevelSnapshot::levelPrice).reversed())
                         .limit(2)
                         .toList();
    }

    private MarketCandidateLevelSnapshot candidate(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelType levelType,
            MarketCandidateLevelLabel levelLabel,
            BigDecimal levelPrice,
            String rationale
    ) {
        return new MarketCandidateLevelSnapshot(
                snapshot.getSymbol(),
                snapshot.getIntervalValue(),
                snapshot.getSnapshotTime(),
                levelType,
                levelLabel,
                sourceType(levelLabel),
                snapshot.getCurrentPrice(),
                levelPrice,
                distanceRatio(snapshot.getCurrentPrice(), levelPrice),
                strengthScore(snapshot, levelLabel, levelPrice),
                rationale,
                triggerFacts(snapshot, levelLabel, levelPrice, levelType),
                buildSourceDataVersion(snapshot, levelType, levelLabel)
        );
    }

    private MarketCandidateLevelSourceType sourceType(MarketCandidateLevelLabel levelLabel) {
        return switch (levelLabel) {
            case MA20, MA60, MA120 -> MarketCandidateLevelSourceType.MOVING_AVERAGE;
            case BB_UPPER, BB_LOWER -> MarketCandidateLevelSourceType.BOLLINGER_BAND;
        };
    }

    private BigDecimal distanceRatio(BigDecimal currentPrice, BigDecimal levelPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return currentPrice.subtract(levelPrice)
                           .abs()
                           .divide(currentPrice, SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal strengthScore(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelLabel levelLabel,
            BigDecimal levelPrice
    ) {
        BigDecimal distancePenalty = distanceRatio(snapshot.getCurrentPrice(), levelPrice);
        BigDecimal baseScore = switch (levelLabel) {
            case MA20, BB_UPPER, BB_LOWER -> new BigDecimal("0.65");
            case MA60 -> new BigDecimal("0.80");
            case MA120 -> new BigDecimal("0.95");
        };
        if (distancePenalty == null) {
            return baseScore;
        }

        BigDecimal score = baseScore.subtract(distancePenalty.min(new BigDecimal("0.40")));
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        }
        return score.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private List<String> triggerFacts(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelLabel levelLabel,
            BigDecimal levelPrice,
            MarketCandidateLevelType levelType
    ) {
        List<String> facts = new ArrayList<>();
        facts.add("Current price " + snapshot.getCurrentPrice().stripTrailingZeros().toPlainString()
                + " vs " + levelLabel.name() + " " + levelPrice.stripTrailingZeros().toPlainString());
        facts.add(levelType.name() + " distance "
                + percent(distanceRatio(snapshot.getCurrentPrice(), levelPrice)));

        switch (levelLabel) {
            case MA20, MA60, MA120 -> facts.add(levelLabel.name() + " captures moving-average trend support/resistance.");
            case BB_UPPER, BB_LOWER -> facts.add(levelLabel.name() + " captures Bollinger band expansion boundary.");
        }

        return facts;
    }

    private String percent(BigDecimal ratio) {
        if (ratio == null) {
            return "unavailable";
        }

        return ratio.multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString() + "%";
    }

    private String buildSourceDataVersion(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelType levelType,
            MarketCandidateLevelLabel levelLabel
    ) {
        return snapshot.getSourceDataVersion()
                + ";levelType=" + levelType.name()
                + ";levelLabel=" + levelLabel.name();
    }
}

package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketCandidateLevelSnapshotService {

    private static final int SCORE_SCALE = 8;
    private static final int PIVOT_LOOKBACK = 2;
    private static final int PIVOT_LOOKFORWARD = 2;
    private static final int MAX_LEVELS_PER_SIDE = 3;

    public List<MarketCandidateLevelSnapshot> createAll(
            MarketIndicatorSnapshotEntity snapshot,
            List<Candle> candles
    ) {
        List<Candle> recentCandles = recentCandles(snapshot, candles);
        BigDecimal priceTolerance = priceTolerance(snapshot);
        BigDecimal recentWindowHigh = recentCandles.stream()
                                                   .map(Candle::high)
                                                   .max(Comparator.naturalOrder())
                                                   .orElse(snapshot.getCurrentPrice());
        BigDecimal recentWindowLow = recentCandles.stream()
                                                  .map(Candle::low)
                                                  .min(Comparator.naturalOrder())
                                                  .orElse(snapshot.getCurrentPrice());

        List<CandidateSeed> candidates = baseCandidates(snapshot);
        candidates.addAll(pivotCandidatesFromLiveCandles(snapshot, recentCandles));

        List<CandidateSeed> enriched = enrichCandidatesFromLiveCandles(candidates, snapshot, recentCandles, recentWindowHigh, recentWindowLow, priceTolerance);

        List<MarketCandidateLevelSnapshot> results = new ArrayList<>();
        results.addAll(selectSupportLevels(snapshot, enriched));
        results.addAll(selectResistanceLevels(snapshot, enriched));
        return results;
    }

    private List<CandidateSeed> baseCandidates(MarketIndicatorSnapshotEntity snapshot) {
        List<CandidateSeed> candidates = new ArrayList<>();
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA20, snapshot.getMa20(), "Short-term average support"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average support"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.MA120, snapshot.getMa120(), "Longer trend average support"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.BB_LOWER, snapshot.getBollingerLowerBand(), "Lower Bollinger band support"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA20, snapshot.getMa20(), "Short-term average resistance"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA60, snapshot.getMa60(), "Mid-trend average resistance"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.MA120, snapshot.getMa120(), "Longer trend average resistance"));
        candidates.add(indicatorCandidate(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.BB_UPPER, snapshot.getBollingerUpperBand(), "Upper Bollinger band resistance"));
        return candidates;
    }

    private List<Candle> recentCandles(
            MarketIndicatorSnapshotEntity snapshot,
            List<Candle> candles
    ) {
        Instant openTimeFrom = snapshot.getSnapshotTime().minus(30, ChronoUnit.DAYS);
        List<Candle> recentCandles = candles.stream()
                .filter(candle -> !candle.openTime().isBefore(openTimeFrom))
                .filter(candle -> !candle.openTime().isAfter(snapshot.getSnapshotTime()))
                .toList();

        if (recentCandles.size() < PIVOT_LOOKBACK + PIVOT_LOOKFORWARD + 1) {
            throw new IllegalStateException(
                    "Not enough raw candles for candidate level snapshot: symbol=%s interval=%s snapshotTime=%s"
                            .formatted(snapshot.getSymbol(), snapshot.getIntervalValue(), snapshot.getSnapshotTime())
            );
        }
        return recentCandles;
    }

    private List<MarketCandidateLevelSnapshot> selectSupportLevels(
            MarketIndicatorSnapshotEntity snapshot,
            List<CandidateSeed> candidates
    ) {
        return candidates.stream()
                         .filter(candidate -> candidate.levelType == MarketCandidateLevelType.SUPPORT)
                         .filter(candidate -> candidate.levelPrice.compareTo(snapshot.getCurrentPrice()) <= 0)
                         .sorted(Comparator.comparing(CandidateSeed::rankingScore).reversed()
                                           .thenComparing(CandidateSeed::levelPrice, Comparator.reverseOrder()))
                         .limit(MAX_LEVELS_PER_SIDE)
                         .map(CandidateSeed::toSnapshot)
                         .toList();
    }

    private List<MarketCandidateLevelSnapshot> selectResistanceLevels(
            MarketIndicatorSnapshotEntity snapshot,
            List<CandidateSeed> candidates
    ) {
        List<CandidateSeed> levels = candidates.stream()
                                               .filter(candidate -> candidate.levelType == MarketCandidateLevelType.RESISTANCE)
                                               .filter(candidate -> candidate.levelPrice.compareTo(snapshot.getCurrentPrice()) >= 0)
                                               .sorted(Comparator.comparing(CandidateSeed::rankingScore).reversed()
                                                                 .thenComparing(CandidateSeed::levelPrice))
                                               .limit(MAX_LEVELS_PER_SIDE)
                                               .toList();
        if (!levels.isEmpty()) {
            return levels.stream().map(CandidateSeed::toSnapshot).toList();
        }

        return candidates.stream()
                         .filter(candidate -> candidate.levelType == MarketCandidateLevelType.RESISTANCE)
                         .sorted(Comparator.comparing(CandidateSeed::rankingScore).reversed()
                                           .thenComparing(CandidateSeed::levelPrice, Comparator.reverseOrder()))
                         .limit(MAX_LEVELS_PER_SIDE)
                         .map(CandidateSeed::toSnapshot)
                         .toList();
    }

    private CandidateSeed indicatorCandidate(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelType levelType,
            MarketCandidateLevelLabel levelLabel,
            BigDecimal levelPrice,
            String rationale
    ) {
        return new CandidateSeed(
                snapshot.getSymbol(),
                snapshot.getIntervalValue(),
                snapshot.getSnapshotTime(),
                snapshot.getSnapshotTime(),
                levelType,
                levelLabel,
                sourceType(levelLabel),
                snapshot.getCurrentPrice(),
                levelPrice,
                0,
                1,
                BigDecimal.ZERO,
                rationale,
                List.of(),
                buildSourceDataVersion(snapshot, levelType, levelLabel, snapshot.getSnapshotTime())
        );
    }

    private List<CandidateSeed> pivotCandidatesFromLiveCandles(
            MarketIndicatorSnapshotEntity snapshot,
            List<Candle> candles
    ) {
        List<CandidateSeed> candidates = new ArrayList<>();
        for (int index = PIVOT_LOOKBACK; index < candles.size() - PIVOT_LOOKFORWARD; index++) {
            Candle current = candles.get(index);
            if (isPivotHighLiveCandle(candles, index)) {
                candidates.add(new CandidateSeed(
                        snapshot.getSymbol(),
                        snapshot.getIntervalValue(),
                        snapshot.getSnapshotTime(),
                        current.openTime(),
                        MarketCandidateLevelType.RESISTANCE,
                        MarketCandidateLevelLabel.PIVOT_HIGH,
                        MarketCandidateLevelSourceType.PIVOT_LEVEL,
                        snapshot.getCurrentPrice(),
                        current.high(),
                        0,
                        1,
                        BigDecimal.ZERO,
                        "Recent pivot high resistance",
                        List.of(),
                        buildSourceDataVersion(snapshot, MarketCandidateLevelType.RESISTANCE, MarketCandidateLevelLabel.PIVOT_HIGH, current.openTime())
                ));
            }
            if (isPivotLowLiveCandle(candles, index)) {
                candidates.add(new CandidateSeed(
                        snapshot.getSymbol(),
                        snapshot.getIntervalValue(),
                        snapshot.getSnapshotTime(),
                        current.openTime(),
                        MarketCandidateLevelType.SUPPORT,
                        MarketCandidateLevelLabel.PIVOT_LOW,
                        MarketCandidateLevelSourceType.PIVOT_LEVEL,
                        snapshot.getCurrentPrice(),
                        current.low(),
                        0,
                        1,
                        BigDecimal.ZERO,
                        "Recent pivot low support",
                        List.of(),
                        buildSourceDataVersion(snapshot, MarketCandidateLevelType.SUPPORT, MarketCandidateLevelLabel.PIVOT_LOW, current.openTime())
                ));
            }
        }
        return candidates;
    }

    private List<CandidateSeed> enrichCandidatesFromLiveCandles(
            List<CandidateSeed> seeds,
            MarketIndicatorSnapshotEntity snapshot,
            List<Candle> candles,
            BigDecimal recentWindowHigh,
            BigDecimal recentWindowLow,
            BigDecimal tolerance
    ) {
        return seeds.stream()
                    .map(seed -> {
                        int reactionCount = reactionCountFromLiveCandles(candles, seed.levelType, seed.levelPrice, tolerance);
                        int clusterSize = clusterSize(seeds, seed.levelType, seed.levelPrice, tolerance);
                        BigDecimal distanceFromCurrent = distanceRatio(snapshot.getCurrentPrice(), seed.levelPrice);
                        BigDecimal proximityScore = proximityScore(seed.levelType, seed.levelPrice, recentWindowHigh, recentWindowLow, tolerance);
                        BigDecimal recencyScore = recencyScore(snapshot.getSnapshotTime(), seed.referenceTime);
                        BigDecimal strengthScore = strengthScore(seed.levelLabel, distanceFromCurrent, reactionCount, clusterSize, proximityScore, recencyScore);
                        List<String> triggerFacts = triggerFacts(
                                snapshot,
                                seed.levelLabel,
                                seed.levelPrice,
                                seed.levelType,
                                reactionCount,
                                clusterSize,
                                proximityScore,
                                recencyScore
                        );
                        return seed.enrich(reactionCount, clusterSize, strengthScore, triggerFacts);
                    })
                    .toList();
    }

    private MarketCandidateLevelSourceType sourceType(MarketCandidateLevelLabel levelLabel) {
        return switch (levelLabel) {
            case MA20, MA60, MA120 -> MarketCandidateLevelSourceType.MOVING_AVERAGE;
            case BB_UPPER, BB_LOWER -> MarketCandidateLevelSourceType.BOLLINGER_BAND;
            case PIVOT_HIGH, PIVOT_LOW -> MarketCandidateLevelSourceType.PIVOT_LEVEL;
        };
    }

    private boolean isPivotHighLiveCandle(List<Candle> candles, int index) {
        BigDecimal high = candles.get(index).high();
        for (int offset = 1; offset <= PIVOT_LOOKBACK; offset++) {
            if (high.compareTo(candles.get(index - offset).high()) <= 0) {
                return false;
            }
        }
        for (int offset = 1; offset <= PIVOT_LOOKFORWARD; offset++) {
            if (high.compareTo(candles.get(index + offset).high()) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isPivotLowLiveCandle(List<Candle> candles, int index) {
        BigDecimal low = candles.get(index).low();
        for (int offset = 1; offset <= PIVOT_LOOKBACK; offset++) {
            if (low.compareTo(candles.get(index - offset).low()) >= 0) {
                return false;
            }
        }
        for (int offset = 1; offset <= PIVOT_LOOKFORWARD; offset++) {
            if (low.compareTo(candles.get(index + offset).low()) > 0) {
                return false;
            }
        }
        return true;
    }

    private int reactionCountFromLiveCandles(
            List<Candle> candles,
            MarketCandidateLevelType levelType,
            BigDecimal levelPrice,
            BigDecimal tolerance
    ) {
        return (int) candles.stream()
                .filter(candle -> touchesLevelLiveCandle(candle, levelType, levelPrice, tolerance))
                .count();
    }

    private boolean touchesLevelLiveCandle(
            Candle candle,
            MarketCandidateLevelType levelType,
            BigDecimal levelPrice,
            BigDecimal tolerance
    ) {
        BigDecimal upperBound = levelPrice.add(tolerance);
        BigDecimal lowerBound = levelPrice.subtract(tolerance);
        if (levelType == MarketCandidateLevelType.SUPPORT) {
            return candle.low().compareTo(upperBound) <= 0
                    && candle.low().compareTo(lowerBound) >= 0;
        }
        return candle.high().compareTo(upperBound) <= 0
                && candle.high().compareTo(lowerBound) >= 0;
    }

    private int clusterSize(
            List<CandidateSeed> seeds,
            MarketCandidateLevelType levelType,
            BigDecimal levelPrice,
            BigDecimal tolerance
    ) {
        return (int) seeds.stream()
                          .filter(seed -> seed.levelType == levelType)
                          .filter(seed -> seed.levelPrice.subtract(levelPrice).abs().compareTo(tolerance) <= 0)
                          .count();
    }

    private BigDecimal proximityScore(
            MarketCandidateLevelType levelType,
            BigDecimal levelPrice,
            BigDecimal recentWindowHigh,
            BigDecimal recentWindowLow,
            BigDecimal tolerance
    ) {
        BigDecimal reference = levelType == MarketCandidateLevelType.SUPPORT ? recentWindowLow : recentWindowHigh;
        BigDecimal distance = reference.subtract(levelPrice).abs();
        if (tolerance.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = BigDecimal.ONE.subtract(distance.divide(tolerance.multiply(new BigDecimal("4")), SCORE_SCALE, RoundingMode.HALF_UP));
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return normalized.min(BigDecimal.ONE).setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal recencyScore(Instant snapshotTime, Instant referenceTime) {
        long hours = Math.max(0, ChronoUnit.HOURS.between(referenceTime, snapshotTime));
        BigDecimal penalty = BigDecimal.valueOf(hours)
                                       .divide(new BigDecimal("720"), SCORE_SCALE, RoundingMode.HALF_UP);
        BigDecimal score = BigDecimal.ONE.subtract(penalty);
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        }
        return score.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
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
            MarketCandidateLevelLabel levelLabel,
            BigDecimal distancePenalty,
            int reactionCount,
            int clusterSize,
            BigDecimal proximityScore,
            BigDecimal recencyScore
    ) {
        BigDecimal baseScore = switch (levelLabel) {
            case MA20, BB_UPPER, BB_LOWER -> new BigDecimal("0.58");
            case MA60 -> new BigDecimal("0.72");
            case MA120 -> new BigDecimal("0.82");
            case PIVOT_HIGH, PIVOT_LOW -> new BigDecimal("0.78");
        };
        BigDecimal reactionBoost = BigDecimal.valueOf(Math.min(reactionCount, 5L))
                                              .multiply(new BigDecimal("0.03"));
        BigDecimal clusterBoost = BigDecimal.valueOf(Math.max(0, clusterSize - 1L))
                                             .multiply(new BigDecimal("0.04"));
        BigDecimal distanceAdjustment = distancePenalty == null
                ? BigDecimal.ZERO
                : distancePenalty.min(new BigDecimal("0.35"));

        BigDecimal score = baseScore
                .add(reactionBoost)
                .add(clusterBoost)
                .add(proximityScore.multiply(new BigDecimal("0.10")))
                .add(recencyScore.multiply(new BigDecimal("0.06")))
                .subtract(distanceAdjustment);

        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
        }
        return score.min(BigDecimal.ONE).setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private List<String> triggerFacts(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelLabel levelLabel,
            BigDecimal levelPrice,
            MarketCandidateLevelType levelType,
            int reactionCount,
            int clusterSize,
            BigDecimal proximityScore,
            BigDecimal recencyScore
    ) {
        List<String> facts = new ArrayList<>();
        facts.add("Current price " + snapshot.getCurrentPrice().stripTrailingZeros().toPlainString()
                + " vs " + levelLabel.name() + " " + levelPrice.stripTrailingZeros().toPlainString());
        facts.add(levelType.name() + " distance " + percent(distanceRatio(snapshot.getCurrentPrice(), levelPrice)));
        facts.add("Reaction count " + reactionCount + " within recent valid candles.");
        facts.add("Cluster size " + clusterSize + " candidate levels near the same price zone.");
        facts.add("Window extremum proximity score " + ratioLabel(proximityScore) + ".");
        facts.add("Reference recency score " + ratioLabel(recencyScore) + ".");

        switch (levelLabel) {
            case MA20, MA60, MA120 -> facts.add(levelLabel.name() + " captures moving-average trend support/resistance.");
            case BB_UPPER, BB_LOWER -> facts.add(levelLabel.name() + " captures Bollinger band expansion boundary.");
            case PIVOT_HIGH -> facts.add("Pivot high marks a recent rejection point and possible overhead supply.");
            case PIVOT_LOW -> facts.add("Pivot low marks a recent defense point and possible demand support.");
        }

        return facts;
    }

    private BigDecimal priceTolerance(MarketIndicatorSnapshotEntity snapshot) {
        BigDecimal atrComponent = snapshot.getAtr14().multiply(new BigDecimal("0.35"));
        BigDecimal ratioComponent = snapshot.getCurrentPrice().multiply(new BigDecimal("0.004"));
        return atrComponent.max(ratioComponent).setScale(SCORE_SCALE, RoundingMode.HALF_UP);
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

    private String ratioLabel(BigDecimal ratio) {
        if (ratio == null) {
            return "unavailable";
        }
        return ratio.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String buildSourceDataVersion(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelType levelType,
            MarketCandidateLevelLabel levelLabel,
            Instant referenceTime
    ) {
        return snapshot.getSourceDataVersion()
                + ";levelType=" + levelType.name()
                + ";levelLabel=" + levelLabel.name()
                + ";referenceTime=" + referenceTime;
    }

    private record CandidateSeed(
            String symbol,
            String intervalValue,
            Instant snapshotTime,
            Instant referenceTime,
            MarketCandidateLevelType levelType,
            MarketCandidateLevelLabel levelLabel,
            MarketCandidateLevelSourceType sourceType,
            BigDecimal currentPrice,
            BigDecimal levelPrice,
            int reactionCount,
            int clusterSize,
            BigDecimal strengthScore,
            String rationale,
            List<String> triggerFacts,
            String sourceDataVersion
    ) {
        private CandidateSeed enrich(
                int reactionCount,
                int clusterSize,
                BigDecimal strengthScore,
                List<String> triggerFacts
        ) {
            return new CandidateSeed(
                    symbol,
                    intervalValue,
                    snapshotTime,
                    referenceTime,
                    levelType,
                    levelLabel,
                    sourceType,
                    currentPrice,
                    levelPrice,
                    reactionCount,
                    clusterSize,
                    strengthScore,
                    rationale,
                    triggerFacts,
                    sourceDataVersion
            );
        }

        private BigDecimal rankingScore() {
            return strengthScore;
        }

        private MarketCandidateLevelSnapshot toSnapshot() {
            return new MarketCandidateLevelSnapshot(
                    symbol,
                    intervalValue,
                    snapshotTime,
                    referenceTime,
                    levelType,
                    levelLabel,
                    sourceType,
                    currentPrice,
                    levelPrice,
                    currentPrice.subtract(levelPrice)
                                .abs()
                                .divide(currentPrice, SCORE_SCALE, RoundingMode.HALF_UP),
                    strengthScore,
                    reactionCount,
                    clusterSize,
                    rationale,
                    triggerFacts,
                    sourceDataVersion
            );
        }
    }
}

package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelZoneSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketCandidateLevelZoneSnapshotService {

    private static final int SCALE = 8;
    private static final int INTERACTION_LOOKBACK_DAYS = 14;

    public List<MarketCandidateLevelZoneSnapshot> createAll(
            List<MarketCandidateLevelSnapshotEntity> levelEntities,
            List<Candle> candles
    ) {
        if (levelEntities.isEmpty()) {
            return List.of();
        }

        List<MarketCandidateLevelZoneSnapshot> zones = new ArrayList<>();
        zones.addAll(createForTypeWithCandles(levelEntities, MarketCandidateLevelType.SUPPORT, candles));
        zones.addAll(createForTypeWithCandles(levelEntities, MarketCandidateLevelType.RESISTANCE, candles));
        return zones;
    }

    private List<MarketCandidateLevelZoneSnapshot> createForTypeWithCandles(
            List<MarketCandidateLevelSnapshotEntity> levelEntities,
            MarketCandidateLevelType zoneType,
            List<Candle> candles
    ) {
        List<MarketCandidateLevelSnapshotEntity> candidates = levelEntities.stream()
                .filter(entity -> entity.getLevelType().equals(zoneType.name()))
                .sorted(Comparator.comparing(MarketCandidateLevelSnapshotEntity::getLevelPrice))
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<Candle> recentCandles = recentCandles(candidates.get(0), candles);

        BigDecimal tolerance = zoneTolerance(candidates.get(0));
        List<List<MarketCandidateLevelSnapshotEntity>> clusters = new ArrayList<>();
        List<MarketCandidateLevelSnapshotEntity> currentCluster = new ArrayList<>();

        for (MarketCandidateLevelSnapshotEntity candidate : candidates) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(candidate);
                continue;
            }

            BigDecimal clusterHigh = currentCluster.stream()
                    .map(MarketCandidateLevelSnapshotEntity::getLevelPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(candidate.getLevelPrice());
            if (candidate.getLevelPrice().subtract(clusterHigh).compareTo(tolerance) <= 0) {
                currentCluster.add(candidate);
                continue;
            }

            clusters.add(List.copyOf(currentCluster));
            currentCluster.clear();
            currentCluster.add(candidate);
        }

        if (!currentCluster.isEmpty()) {
            clusters.add(List.copyOf(currentCluster));
        }

        List<ZoneDraft> drafts = clusters.stream()
                .map(cluster -> toZoneDraftFromLiveCandles(zoneType, cluster, recentCandles))
                .sorted(zoneComparator(zoneType))
                .toList();

        List<MarketCandidateLevelZoneSnapshot> snapshots = new ArrayList<>();
        for (int index = 0; index < drafts.size(); index++) {
            snapshots.add(drafts.get(index).toSnapshot(index + 1));
        }
        return snapshots;
    }

    private Comparator<ZoneDraft> zoneComparator(MarketCandidateLevelType zoneType) {
        return switch (zoneType) {
            case SUPPORT -> Comparator.comparing(ZoneDraft::representativePrice, Comparator.reverseOrder());
            case RESISTANCE -> Comparator.comparing(ZoneDraft::representativePrice);
        };
    }

    private ZoneDraft toZoneDraftFromLiveCandles(
            MarketCandidateLevelType zoneType,
            List<MarketCandidateLevelSnapshotEntity> cluster,
            List<Candle> recentCandles
    ) {
        MarketCandidateLevelSnapshotEntity strongest = cluster.stream()
                .max(Comparator.comparing(MarketCandidateLevelSnapshotEntity::getStrengthScore)
                        .thenComparing(MarketCandidateLevelSnapshotEntity::getClusterSize)
                        .thenComparing(MarketCandidateLevelSnapshotEntity::getReactionCount))
                .orElseThrow();
        BigDecimal zoneLow = cluster.stream()
                .map(MarketCandidateLevelSnapshotEntity::getLevelPrice)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        BigDecimal zoneHigh = cluster.stream()
                .map(MarketCandidateLevelSnapshotEntity::getLevelPrice)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        Set<String> includedLevelLabels = cluster.stream()
                .map(MarketCandidateLevelSnapshotEntity::getLevelLabel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> includedSourceTypes = cluster.stream()
                .map(MarketCandidateLevelSnapshotEntity::getSourceType)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        ZoneInteraction interaction = zoneInteractionFromLiveCandles(zoneType, strongest.getCurrentPrice(), zoneLow, zoneHigh, recentCandles);
        List<String> triggerFacts = zoneTriggerFacts(zoneType, cluster, strongest, zoneLow, zoneHigh, interaction);

        return new ZoneDraft(
                strongest.getSymbol(),
                strongest.getIntervalValue(),
                strongest.getSnapshotTime(),
                zoneType,
                strongest.getCurrentPrice(),
                strongest.getLevelPrice(),
                zoneLow,
                zoneHigh,
                strongest.getDistanceFromCurrent(),
                interaction.distanceToZone(),
                zoneStrengthScore(cluster, strongest, interaction),
                interaction.interactionType(),
                MarketCandidateLevelLabel.valueOf(strongest.getLevelLabel()),
                MarketCandidateLevelSourceType.valueOf(strongest.getSourceType()),
                cluster.size(),
                interaction.recentTestCount(),
                interaction.recentRejectionCount(),
                interaction.recentBreakCount(),
                includedLevelLabels.stream().map(MarketCandidateLevelLabel::valueOf).toList(),
                includedSourceTypes.stream().map(MarketCandidateLevelSourceType::valueOf).toList(),
                triggerFacts,
                buildSourceDataVersion(zoneType, cluster, zoneLow, zoneHigh)
        );
    }

    private List<Candle> recentCandles(MarketCandidateLevelSnapshotEntity anchor, List<Candle> candles) {
        Instant openTimeFrom = anchor.getSnapshotTime().minus(INTERACTION_LOOKBACK_DAYS, ChronoUnit.DAYS);
        return candles.stream()
                .filter(candle -> !candle.openTime().isBefore(openTimeFrom))
                .filter(candle -> !candle.openTime().isAfter(anchor.getSnapshotTime()))
                .toList();
    }

    private BigDecimal zoneTolerance(MarketCandidateLevelSnapshotEntity anchor) {
        return anchor.getCurrentPrice()
                     .multiply(new BigDecimal("0.006"))
                     .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal zoneStrengthScore(
            List<MarketCandidateLevelSnapshotEntity> cluster,
            MarketCandidateLevelSnapshotEntity strongest,
            ZoneInteraction interaction
    ) {
        BigDecimal clusterBoost = BigDecimal.valueOf(Math.max(0L, cluster.size() - 1L))
                                            .multiply(new BigDecimal("0.05"));
        BigDecimal reactionBoost = BigDecimal.valueOf(
                cluster.stream()
                       .map(MarketCandidateLevelSnapshotEntity::getReactionCount)
                       .reduce(0, Integer::sum)
        ).min(new BigDecimal("8")).multiply(new BigDecimal("0.01"));
        BigDecimal testBoost = BigDecimal.valueOf(Math.min(interaction.recentTestCount(), 5))
                                         .multiply(new BigDecimal("0.02"));
        BigDecimal rejectionBoost = BigDecimal.valueOf(Math.min(interaction.recentRejectionCount(), 4))
                                              .multiply(new BigDecimal("0.03"));
        BigDecimal breakPenalty = BigDecimal.valueOf(Math.min(interaction.recentBreakCount(), 4))
                                             .multiply(new BigDecimal("0.04"));
        BigDecimal distancePenalty = interaction.distanceToZone() == null
                ? BigDecimal.ZERO
                : interaction.distanceToZone().min(new BigDecimal("0.08"));
        BigDecimal score = strongest.getStrengthScore()
                                    .add(clusterBoost)
                                    .add(reactionBoost)
                                    .add(testBoost)
                                    .add(rejectionBoost)
                                    .subtract(breakPenalty)
                                    .subtract(distancePenalty);
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return score.min(BigDecimal.ONE).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private List<String> zoneTriggerFacts(
            MarketCandidateLevelType zoneType,
            List<MarketCandidateLevelSnapshotEntity> cluster,
            MarketCandidateLevelSnapshotEntity strongest,
            BigDecimal zoneLow,
            BigDecimal zoneHigh,
            ZoneInteraction interaction
    ) {
        List<String> facts = new ArrayList<>();
        if (zoneLow.compareTo(zoneHigh) == 0) {
            facts.add("%s zone is a single calculated level at %s with %d candidate levels."
                              .formatted(
                                      zoneType.name(),
                                      zoneLow.stripTrailingZeros().toPlainString(),
                                      cluster.size()
                              ));
        } else {
            facts.add("%s zone spans %s to %s with %d candidate levels."
                              .formatted(
                                      zoneType.name(),
                                      zoneLow.stripTrailingZeros().toPlainString(),
                                      zoneHigh.stripTrailingZeros().toPlainString(),
                                      cluster.size()
                              ));
        }
        facts.add("Strongest level is %s from %s at %s."
                          .formatted(
                                  strongest.getLevelLabel(),
                                  strongest.getSourceType().toLowerCase(Locale.ROOT).replace('_', ' '),
                                  strongest.getLevelPrice().stripTrailingZeros().toPlainString()
                          ));
        facts.add("Zone combines labels %s."
                          .formatted(
                                  cluster.stream()
                                         .map(MarketCandidateLevelSnapshotEntity::getLevelLabel)
                                         .distinct()
                                         .collect(Collectors.joining(", "))
                          ));
        facts.add("Current price is %s the zone by %s."
                          .formatted(
                                  interaction.interactionType().name().toLowerCase(Locale.ROOT).replace('_', ' '),
                                  percent(interaction.distanceToZone())
                          ));
        facts.add("Recent tests=%d, rejections=%d, breaks=%d within %d days."
                          .formatted(
                                  interaction.recentTestCount(),
                                  interaction.recentRejectionCount(),
                                  interaction.recentBreakCount(),
                                  INTERACTION_LOOKBACK_DAYS
                          ));
        cluster.stream()
               .flatMap(entity -> extractEmbeddedFacts(entity.getTriggerFactsPayload()).stream())
               .distinct()
               .limit(3)
               .forEach(facts::add);
        return facts;
    }

    private ZoneInteraction zoneInteractionFromLiveCandles(
            MarketCandidateLevelType zoneType,
            BigDecimal currentPrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh,
            List<Candle> recentCandles
    ) {
        MarketCandidateLevelZoneInteractionType interactionType = interactionType(currentPrice, zoneLow, zoneHigh);
        BigDecimal distanceToZone = distanceToZone(currentPrice, zoneLow, zoneHigh);
        int recentTestCount = (int) recentCandles.stream()
                .filter(candle -> touchesZone(candle, zoneLow, zoneHigh))
                .count();
        int recentRejectionCount = (int) recentCandles.stream()
                .filter(candle -> touchesZone(candle, zoneLow, zoneHigh))
                .filter(candle -> rejectedFromZone(zoneType, candle, zoneLow, zoneHigh))
                .count();
        int recentBreakCount = (int) recentCandles.stream()
                .filter(candle -> breaksZone(zoneType, candle, zoneLow, zoneHigh))
                .count();
        return new ZoneInteraction(interactionType, distanceToZone, recentTestCount, recentRejectionCount, recentBreakCount);
    }

    private MarketCandidateLevelZoneInteractionType interactionType(
            BigDecimal currentPrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        if (currentPrice.compareTo(zoneLow) >= 0 && currentPrice.compareTo(zoneHigh) <= 0) {
            return MarketCandidateLevelZoneInteractionType.INSIDE_ZONE;
        }
        if (currentPrice.compareTo(zoneHigh) > 0) {
            return MarketCandidateLevelZoneInteractionType.ABOVE_ZONE;
        }
        return MarketCandidateLevelZoneInteractionType.BELOW_ZONE;
    }

    private BigDecimal distanceToZone(
            BigDecimal currentPrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        if (currentPrice.compareTo(zoneLow) >= 0 && currentPrice.compareTo(zoneHigh) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal boundary = currentPrice.compareTo(zoneLow) < 0 ? zoneLow : zoneHigh;
        return currentPrice.subtract(boundary)
                           .abs()
                           .divide(currentPrice, SCALE, RoundingMode.HALF_UP);
    }

    private boolean touchesZone(Candle candle, BigDecimal zoneLow, BigDecimal zoneHigh) {
        return candle.high().compareTo(zoneLow) >= 0
                && candle.low().compareTo(zoneHigh) <= 0;
    }

    private boolean rejectedFromZone(
            MarketCandidateLevelType zoneType,
            Candle candle,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        return switch (zoneType) {
            case SUPPORT -> candle.close().compareTo(zoneHigh) >= 0;
            case RESISTANCE -> candle.close().compareTo(zoneLow) <= 0;
        };
    }

    private boolean breaksZone(
            MarketCandidateLevelType zoneType,
            Candle candle,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        return switch (zoneType) {
            case SUPPORT -> candle.close().compareTo(zoneLow) < 0;
            case RESISTANCE -> candle.close().compareTo(zoneHigh) > 0;
        };
    }

    private List<String> extractEmbeddedFacts(String triggerFactsPayload) {
        String normalized = triggerFactsPayload
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        if (normalized.isBlank()) {
            return List.of();
        }
        return List.of(normalized.split(","))
                   .stream()
                   .map(String::trim)
                   .filter(part -> !part.isBlank())
                   .toList();
    }

    private String buildSourceDataVersion(
            MarketCandidateLevelType zoneType,
            List<MarketCandidateLevelSnapshotEntity> cluster,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        String indicatorVersion = cluster.stream()
                                         .map(MarketCandidateLevelSnapshotEntity::getSourceDataVersion)
                                         .map(this::extractIndicatorVersion)
                                         .distinct()
                                         .sorted()
                                         .collect(Collectors.joining("|"));
        String memberSummary = cluster.stream()
                                      .map(entity -> entity.getLevelLabel()
                                              + "@"
                                              + entity.getReferenceTime()
                                              + "#"
                                              + entity.getSourceType())
                                      .sorted()
                                      .collect(Collectors.joining("|"));
        String digest = sha256Hex(
                cluster.stream()
                       .map(MarketCandidateLevelSnapshotEntity::getSourceDataVersion)
                       .sorted()
                       .collect(Collectors.joining("|"))
        ).substring(0, 16);
        return "indicator=" + indicatorVersion
                + ";members=" + memberSummary
                + ";digest=" + digest
                + ";zoneType=" + zoneType.name()
                + ";zoneLow=" + zoneLow.stripTrailingZeros().toPlainString()
                + ";zoneHigh=" + zoneHigh.stripTrailingZeros().toPlainString();
    }

    private String extractIndicatorVersion(String sourceDataVersion) {
        int levelTypeIndex = sourceDataVersion.indexOf(";levelType=");
        if (levelTypeIndex < 0) {
            return sourceDataVersion;
        }
        return sourceDataVersion.substring(0, levelTypeIndex);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
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

    private record ZoneDraft(
            String symbol,
            String intervalValue,
            java.time.Instant snapshotTime,
            MarketCandidateLevelType zoneType,
            BigDecimal currentPrice,
            BigDecimal representativePrice,
            BigDecimal zoneLow,
            BigDecimal zoneHigh,
            BigDecimal distanceFromCurrent,
            BigDecimal distanceToZone,
            BigDecimal zoneStrengthScore,
            MarketCandidateLevelZoneInteractionType interactionType,
            MarketCandidateLevelLabel strongestLevelLabel,
            MarketCandidateLevelSourceType strongestSourceType,
            Integer levelCount,
            Integer recentTestCount,
            Integer recentRejectionCount,
            Integer recentBreakCount,
            List<MarketCandidateLevelLabel> includedLevelLabels,
            List<MarketCandidateLevelSourceType> includedSourceTypes,
            List<String> triggerFacts,
            String sourceDataVersion
    ) {
        private MarketCandidateLevelZoneSnapshot toSnapshot(int zoneRank) {
            return new MarketCandidateLevelZoneSnapshot(
                    symbol,
                    intervalValue,
                    snapshotTime,
                    zoneType,
                    zoneRank,
                    currentPrice,
                    representativePrice,
                    zoneLow,
                    zoneHigh,
                    distanceFromCurrent,
                    distanceToZone,
                    zoneStrengthScore,
                    interactionType,
                    strongestLevelLabel,
                    strongestSourceType,
                    levelCount,
                    recentTestCount,
                    recentRejectionCount,
                    recentBreakCount,
                    includedLevelLabels,
                    includedSourceTypes,
                    triggerFacts,
                    sourceDataVersion + ";zoneRank=" + zoneRank
            );
        }
    }

    private record ZoneInteraction(
            MarketCandidateLevelZoneInteractionType interactionType,
            BigDecimal distanceToZone,
            Integer recentTestCount,
            Integer recentRejectionCount,
            Integer recentBreakCount
    ) {
    }
}

package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelZoneSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MarketCandidateLevelZoneSnapshotService {

    private static final int SCALE = 8;

    public List<MarketCandidateLevelZoneSnapshot> createAll(List<MarketCandidateLevelSnapshotEntity> levelEntities) {
        if (levelEntities.isEmpty()) {
            return List.of();
        }

        List<MarketCandidateLevelZoneSnapshot> zones = new ArrayList<>();
        zones.addAll(createForType(levelEntities, MarketCandidateLevelType.SUPPORT));
        zones.addAll(createForType(levelEntities, MarketCandidateLevelType.RESISTANCE));
        return zones;
    }

    private List<MarketCandidateLevelZoneSnapshot> createForType(
            List<MarketCandidateLevelSnapshotEntity> levelEntities,
            MarketCandidateLevelType zoneType
    ) {
        List<MarketCandidateLevelSnapshotEntity> candidates = levelEntities.stream()
                                                                           .filter(entity -> entity.getLevelType().equals(zoneType.name()))
                                                                           .sorted(Comparator.comparing(MarketCandidateLevelSnapshotEntity::getLevelPrice))
                                                                           .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }

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
                                         .map(cluster -> toZoneDraft(zoneType, cluster))
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

    private ZoneDraft toZoneDraft(
            MarketCandidateLevelType zoneType,
            List<MarketCandidateLevelSnapshotEntity> cluster
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
        List<String> triggerFacts = zoneTriggerFacts(zoneType, cluster, strongest, zoneLow, zoneHigh);

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
                zoneStrengthScore(cluster, strongest),
                MarketCandidateLevelLabel.valueOf(strongest.getLevelLabel()),
                MarketCandidateLevelSourceType.valueOf(strongest.getSourceType()),
                cluster.size(),
                includedLevelLabels.stream().map(MarketCandidateLevelLabel::valueOf).toList(),
                includedSourceTypes.stream().map(MarketCandidateLevelSourceType::valueOf).toList(),
                triggerFacts,
                buildSourceDataVersion(zoneType, cluster, zoneLow, zoneHigh)
        );
    }

    private BigDecimal zoneTolerance(MarketCandidateLevelSnapshotEntity anchor) {
        return anchor.getCurrentPrice()
                     .multiply(new BigDecimal("0.006"))
                     .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal zoneStrengthScore(
            List<MarketCandidateLevelSnapshotEntity> cluster,
            MarketCandidateLevelSnapshotEntity strongest
    ) {
        BigDecimal clusterBoost = BigDecimal.valueOf(Math.max(0L, cluster.size() - 1L))
                                            .multiply(new BigDecimal("0.05"));
        BigDecimal reactionBoost = BigDecimal.valueOf(
                cluster.stream()
                       .map(MarketCandidateLevelSnapshotEntity::getReactionCount)
                       .reduce(0, Integer::sum)
        ).min(new BigDecimal("8")).multiply(new BigDecimal("0.01"));
        BigDecimal score = strongest.getStrengthScore().add(clusterBoost).add(reactionBoost);
        return score.min(BigDecimal.ONE).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private List<String> zoneTriggerFacts(
            MarketCandidateLevelType zoneType,
            List<MarketCandidateLevelSnapshotEntity> cluster,
            MarketCandidateLevelSnapshotEntity strongest,
            BigDecimal zoneLow,
            BigDecimal zoneHigh
    ) {
        List<String> facts = new ArrayList<>();
        facts.add("%s zone spans %s to %s with %d candidate levels."
                          .formatted(
                                  zoneType.name(),
                                  zoneLow.stripTrailingZeros().toPlainString(),
                                  zoneHigh.stripTrailingZeros().toPlainString(),
                                  cluster.size()
                          ));
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
        cluster.stream()
               .flatMap(entity -> extractEmbeddedFacts(entity.getTriggerFactsPayload()).stream())
               .distinct()
               .limit(3)
               .forEach(facts::add);
        return facts;
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
        String baseVersion = cluster.stream()
                                    .map(MarketCandidateLevelSnapshotEntity::getSourceDataVersion)
                                    .sorted()
                                    .collect(Collectors.joining("|"));
        return baseVersion
                + ";zoneType=" + zoneType.name()
                + ";zoneLow=" + zoneLow.stripTrailingZeros().toPlainString()
                + ";zoneHigh=" + zoneHigh.stripTrailingZeros().toPlainString();
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
            BigDecimal zoneStrengthScore,
            MarketCandidateLevelLabel strongestLevelLabel,
            MarketCandidateLevelSourceType strongestSourceType,
            Integer levelCount,
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
                    zoneStrengthScore,
                    strongestLevelLabel,
                    strongestSourceType,
                    levelCount,
                    includedLevelLabels,
                    includedSourceTypes,
                    triggerFacts,
                    sourceDataVersion + ";zoneRank=" + zoneRank
            );
        }
    }
}

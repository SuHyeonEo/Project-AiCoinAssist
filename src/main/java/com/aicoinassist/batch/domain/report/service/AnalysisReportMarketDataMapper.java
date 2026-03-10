package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisReportMarketDataMapper {

    private final ObjectMapper objectMapper;

    public AnalysisWindowSummary toWindowSummary(MarketWindowSummarySnapshotEntity entity) {
        return new AnalysisWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getWindowHigh(),
                entity.getWindowLow(),
                entity.getWindowRange(),
                entity.getCurrentPositionInRange(),
                entity.getDistanceFromWindowHigh(),
                entity.getReboundFromWindowLow(),
                entity.getAverageVolume(),
                entity.getAverageAtr(),
                entity.getCurrentVolumeVsAverage(),
                entity.getCurrentAtrVsAverage()
        );
    }

    public AnalysisDerivativeContext toDerivativeContext(
            MarketContextSnapshotEntity entity,
            List<AnalysisDerivativeComparisonFact> derivativeComparisonFacts,
            List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries
    ) {
        return new AnalysisDerivativeContext(
                entity.getSnapshotTime(),
                entity.getOpenInterestSourceEventTime(),
                entity.getPremiumIndexSourceEventTime(),
                entity.getSourceDataVersion(),
                entity.getOpenInterest(),
                entity.getMarkPrice(),
                entity.getIndexPrice(),
                entity.getLastFundingRate(),
                entity.getNextFundingTime(),
                entity.getMarkIndexBasisRate(),
                derivativeComparisonFacts,
                derivativeWindowSummaries,
                List.of()
        );
    }

    public AnalysisDerivativeWindowSummary toDerivativeWindowSummary(MarketContextWindowSummarySnapshotEntity entity) {
        return new AnalysisDerivativeWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getAverageOpenInterest(),
                entity.getCurrentOpenInterestVsAverage(),
                entity.getAverageFundingRate(),
                entity.getCurrentFundingVsAverage(),
                entity.getAverageBasisRate(),
                entity.getCurrentBasisVsAverage()
        );
    }

    public List<AnalysisPriceLevel> toCandidateLevels(
            List<MarketCandidateLevelSnapshotEntity> entities,
            String levelType,
            Comparator<AnalysisPriceLevel> comparator
    ) {
        return entities.stream()
                       .filter(entity -> entity.getLevelType().equals(levelType))
                       .map(this::toPriceLevel)
                       .sorted(comparator)
                       .toList();
    }

    public List<AnalysisPriceZone> toCandidateZones(
            List<MarketCandidateLevelZoneSnapshotEntity> entities,
            String zoneType
    ) {
        return entities.stream()
                       .filter(entity -> entity.getZoneType().equals(zoneType))
                       .sorted(Comparator.comparing(MarketCandidateLevelZoneSnapshotEntity::getZoneRank))
                       .map(this::toPriceZone)
                       .toList();
    }

    public AnalysisLevelContextPayload toLevelContext(
            MarketLevelContextSnapshotEntity entity,
            List<AnalysisPriceZone> supportZones,
            List<AnalysisPriceZone> resistanceZones,
            List<AnalysisLevelContextComparisonFact> comparisonFacts
    ) {
        AnalysisPriceZone nearestSupportZone = nearestZone(supportZones, entity.getSupportZoneRank());
        AnalysisPriceZone nearestResistanceZone = nearestZone(resistanceZones, entity.getResistanceZoneRank());
        return new AnalysisLevelContextPayload(
                nearestSupportZone,
                nearestResistanceZone,
                zoneInteractionFacts(entity, nearestSupportZone, nearestResistanceZone),
                entity.getSupportBreakRisk(),
                entity.getResistanceBreakRisk(),
                comparisonFacts,
                List.of()
        );
    }

    private AnalysisPriceLevel toPriceLevel(MarketCandidateLevelSnapshotEntity entity) {
        return new AnalysisPriceLevel(
                AnalysisPriceLevelLabel.valueOf(entity.getLevelLabel()),
                AnalysisPriceLevelSourceType.valueOf(entity.getSourceType()),
                entity.getLevelPrice(),
                entity.getDistanceFromCurrent(),
                entity.getStrengthScore(),
                entity.getReferenceTime(),
                entity.getReactionCount(),
                entity.getClusterSize(),
                entity.getRationale(),
                stringListPayload(entity.getTriggerFactsPayload(), "Failed to deserialize market candidate level trigger facts.")
        );
    }

    private AnalysisPriceZone toPriceZone(MarketCandidateLevelZoneSnapshotEntity entity) {
        return new AnalysisPriceZone(
                AnalysisPriceZoneType.valueOf(entity.getZoneType()),
                entity.getZoneRank(),
                entity.getRepresentativePrice(),
                entity.getZoneLow(),
                entity.getZoneHigh(),
                entity.getDistanceFromCurrent(),
                entity.getDistanceToZone(),
                entity.getZoneStrengthScore(),
                AnalysisPriceZoneInteractionType.valueOf(entity.getInteractionType()),
                AnalysisPriceLevelLabel.valueOf(entity.getStrongestLevelLabel()),
                AnalysisPriceLevelSourceType.valueOf(entity.getStrongestSourceType()),
                entity.getLevelCount(),
                entity.getRecentTestCount(),
                entity.getRecentRejectionCount(),
                entity.getRecentBreakCount(),
                enumPayload(entity.getIncludedLevelLabelsPayload(), AnalysisPriceLevelLabel.class),
                enumPayload(entity.getIncludedSourceTypesPayload(), AnalysisPriceLevelSourceType.class),
                stringListPayload(entity.getTriggerFactsPayload(), "Failed to deserialize market candidate level zone trigger facts.")
        );
    }

    private AnalysisPriceZone nearestZone(List<AnalysisPriceZone> zones, Integer zoneRank) {
        if (zoneRank == null) {
            return null;
        }
        return zones.stream()
                    .filter(zone -> zone.zoneRank().equals(zoneRank))
                    .findFirst()
                    .orElse(null);
    }

    private List<AnalysisZoneInteractionFact> zoneInteractionFacts(
            MarketLevelContextSnapshotEntity entity,
            AnalysisPriceZone nearestSupportZone,
            AnalysisPriceZone nearestResistanceZone
    ) {
        List<AnalysisZoneInteractionFact> facts = new ArrayList<>();
        if (nearestSupportZone != null && entity.getSupportInteractionType() != null) {
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.SUPPORT,
                    entity.getSupportZoneRank(),
                    AnalysisPriceZoneInteractionType.valueOf(entity.getSupportInteractionType()),
                    "Nearest support zone is %s to %s, currently %s with %d tests and break risk %s."
                            .formatted(
                                    nearestSupportZone.zoneLow().stripTrailingZeros().toPlainString(),
                                    nearestSupportZone.zoneHigh().stripTrailingZeros().toPlainString(),
                                    entity.getSupportInteractionType().toLowerCase().replace('_', ' '),
                                    zeroSafe(entity.getSupportRecentTestCount()),
                                    percentage(entity.getSupportBreakRisk())
                            ),
                    nearestSupportZone.triggerFacts()
            ));
        }
        if (nearestResistanceZone != null && entity.getResistanceInteractionType() != null) {
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.RESISTANCE,
                    entity.getResistanceZoneRank(),
                    AnalysisPriceZoneInteractionType.valueOf(entity.getResistanceInteractionType()),
                    "Nearest resistance zone is %s to %s, currently %s with %d tests and break risk %s."
                            .formatted(
                                    nearestResistanceZone.zoneLow().stripTrailingZeros().toPlainString(),
                                    nearestResistanceZone.zoneHigh().stripTrailingZeros().toPlainString(),
                                    entity.getResistanceInteractionType().toLowerCase().replace('_', ' '),
                                    zeroSafe(entity.getResistanceRecentTestCount()),
                                    percentage(entity.getResistanceBreakRisk())
                            ),
                    nearestResistanceZone.triggerFacts()
            ));
        }
        return facts;
    }

    private int zeroSafe(Integer value) {
        return value == null ? 0 : value;
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

    private List<String> stringListPayload(String payload, String message) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalStateException(message, exception);
        }
    }

    private <T extends Enum<T>> List<T> enumPayload(String payload, Class<T> enumClass) {
        try {
            List<String> names = objectMapper.readValue(payload, new TypeReference<>() {});
            return names.stream().map(name -> Enum.valueOf(enumClass, name)).toList();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize market candidate level zone enum payload.", exception);
        }
    }
}

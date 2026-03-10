package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelZoneSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketLevelContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportGenerationService {

    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;
    private final MarketCandidateLevelSnapshotPersistenceService marketCandidateLevelSnapshotPersistenceService;
    private final MarketCandidateLevelZoneSnapshotPersistenceService marketCandidateLevelZoneSnapshotPersistenceService;
    private final MarketLevelContextSnapshotPersistenceService marketLevelContextSnapshotPersistenceService;
    private final MarketContextSnapshotPersistenceService marketContextSnapshotPersistenceService;
    private final MarketContextWindowSummarySnapshotPersistenceService marketContextWindowSummarySnapshotPersistenceService;
    private final MarketWindowSummarySnapshotPersistenceService marketWindowSummarySnapshotPersistenceService;
    private final AnalysisComparisonService analysisComparisonService;
    private final AnalysisDerivativeComparisonService analysisDerivativeComparisonService;
    private final AnalysisReportContinuityService analysisReportContinuityService;
    private final AnalysisReportAssembler analysisReportAssembler;
    private final AnalysisReportPersistenceService analysisReportPersistenceService;
    private final ObjectMapper objectMapper;

    public AnalysisReportEntity generateAndSave(
            String symbol,
            AnalysisReportType reportType,
            String analysisEngineVersion,
            Instant storedTime
    ) {
        CandleInterval interval = intervalFor(reportType);
        MarketIndicatorSnapshotEntity snapshot = marketIndicatorSnapshotRepository
                .findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc(symbol, interval.value())
                .orElseThrow(() -> new IllegalStateException(
                        "No market indicator snapshot found for symbol=%s interval=%s".formatted(symbol, interval.value())
                ));

        List<AnalysisComparisonFact> comparisonFacts = analysisComparisonService.buildFacts(snapshot, reportType);
        List<AnalysisContinuityNote> continuityNotes = analysisReportContinuityService.buildNotes(
                symbol,
                reportType,
                snapshot.getSnapshotTime()
        );
        MarketContextSnapshotEntity marketContextSnapshot = marketContextSnapshotPersistenceService.createAndSave(symbol);
        List<AnalysisDerivativeComparisonFact> derivativeComparisonFacts = analysisDerivativeComparisonService.buildFacts(
                marketContextSnapshot,
                reportType
        );
        List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries = marketContextWindowSummarySnapshotPersistenceService
                .createAndSaveForReportType(marketContextSnapshot, reportType)
                .stream()
                .map(this::toDerivativeWindowSummary)
                .toList();
        AnalysisDerivativeContext derivativeContext = toDerivativeContext(
                marketContextSnapshot,
                derivativeComparisonFacts,
                derivativeWindowSummaries
        );
        List<AnalysisWindowSummary> windowSummaries = marketWindowSummarySnapshotPersistenceService
                .createAndSaveForReportType(snapshot, reportType)
                .stream()
                .map(this::toWindowSummary)
                .toList();
        List<MarketCandidateLevelSnapshotEntity> candidateLevelSnapshots = marketCandidateLevelSnapshotPersistenceService
                .createAndSaveAll(snapshot);
        List<MarketCandidateLevelZoneSnapshotEntity> candidateLevelZoneSnapshots = marketCandidateLevelZoneSnapshotPersistenceService
                .createAndSaveAll(candidateLevelSnapshots);
        List<AnalysisPriceLevel> supportLevels = candidateLevels(candidateLevelSnapshots, "SUPPORT", Comparator.comparing(AnalysisPriceLevel::price).reversed());
        List<AnalysisPriceLevel> resistanceLevels = candidateLevels(candidateLevelSnapshots, "RESISTANCE", Comparator.comparing(AnalysisPriceLevel::price));
        List<AnalysisPriceZone> supportZones = candidateZones(candidateLevelZoneSnapshots, "SUPPORT");
        List<AnalysisPriceZone> resistanceZones = candidateZones(candidateLevelZoneSnapshots, "RESISTANCE");
        MarketLevelContextSnapshotEntity levelContextSnapshot = marketLevelContextSnapshotPersistenceService
                .createAndSave(snapshot, candidateLevelZoneSnapshots);
        AnalysisLevelContextPayload levelContext = toLevelContext(levelContextSnapshot, supportZones, resistanceZones);
        AnalysisReportPayload payload = analysisReportAssembler.assemble(
                snapshot,
                reportType,
                comparisonFacts,
                windowSummaries,
                derivativeContext,
                continuityNotes,
                levelContext,
                supportLevels,
                resistanceLevels,
                supportZones,
                resistanceZones
        );
        AnalysisReportDraft draft = new AnalysisReportDraft(
                snapshot.getSymbol(),
                reportType,
                snapshot.getSnapshotTime(),
                snapshot.getPriceSourceEventTime(),
                snapshot.getSourceDataVersion(),
                analysisEngineVersion,
                payload,
                storedTime
        );

        return analysisReportPersistenceService.save(draft);
    }

    private CandleInterval intervalFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> CandleInterval.ONE_HOUR;
            case MID_TERM -> CandleInterval.FOUR_HOUR;
            case LONG_TERM -> CandleInterval.ONE_DAY;
        };
    }

    private AnalysisWindowSummary toWindowSummary(MarketWindowSummarySnapshotEntity entity) {
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

    private AnalysisDerivativeContext toDerivativeContext(
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

    private AnalysisDerivativeWindowSummary toDerivativeWindowSummary(MarketContextWindowSummarySnapshotEntity entity) {
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

    private AnalysisLevelContextPayload toLevelContext(
            MarketLevelContextSnapshotEntity entity,
            List<AnalysisPriceZone> supportZones,
            List<AnalysisPriceZone> resistanceZones
    ) {
        AnalysisPriceZone nearestSupportZone = nearestZone(supportZones, entity.getSupportZoneRank());
        AnalysisPriceZone nearestResistanceZone = nearestZone(resistanceZones, entity.getResistanceZoneRank());
        return new AnalysisLevelContextPayload(
                nearestSupportZone,
                nearestResistanceZone,
                zoneInteractionFacts(entity, nearestSupportZone, nearestResistanceZone),
                entity.getSupportBreakRisk(),
                entity.getResistanceBreakRisk()
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
        java.util.ArrayList<AnalysisZoneInteractionFact> facts = new java.util.ArrayList<>();
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

    private String percentage(java.math.BigDecimal value) {
        if (value == null) {
            return "unavailable";
        }
        return value.multiply(new java.math.BigDecimal("100"))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString() + "%";
    }

    private List<AnalysisPriceLevel> candidateLevels(
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
                triggerFacts(entity.getTriggerFactsPayload())
        );
    }

    private List<AnalysisPriceZone> candidateZones(
            List<MarketCandidateLevelZoneSnapshotEntity> entities,
            String zoneType
    ) {
        return entities.stream()
                       .filter(entity -> entity.getZoneType().equals(zoneType))
                       .sorted(Comparator.comparing(MarketCandidateLevelZoneSnapshotEntity::getZoneRank))
                       .map(this::toPriceZone)
                       .toList();
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
                triggerFacts(entity.getTriggerFactsPayload())
        );
    }

    private List<String> triggerFacts(String triggerFactsPayload) {
        try {
            return objectMapper.readValue(triggerFactsPayload, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize market candidate level trigger facts.", exception);
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

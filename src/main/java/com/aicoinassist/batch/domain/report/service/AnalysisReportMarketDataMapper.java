package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimePersistence;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeTransition;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeSignal;
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
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentWindowSummarySnapshotEntity;
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
    private final AnalysisReportFormattingSupport formattingSupport = new AnalysisReportFormattingSupport();
    private final AnalysisTextLocalizationSupport textLocalizationSupport = new AnalysisTextLocalizationSupport();

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
                entity.getAverageQuoteAssetVolume(),
                entity.getAverageTradeCount(),
                entity.getAverageAtr(),
                entity.getCurrentVolumeVsAverage(),
                entity.getCurrentQuoteAssetVolumeVsAverage(),
                entity.getCurrentTradeCountVsAverage(),
                entity.getCurrentTakerBuyQuoteRatio(),
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

    public AnalysisSentimentContext toSentimentContext(
            SentimentSnapshotEntity entity,
            List<AnalysisSentimentComparisonFact> comparisonFacts,
            List<AnalysisSentimentWindowSummary> windowSummaries
    ) {
        return new AnalysisSentimentContext(
                entity.getSnapshotTime(),
                entity.getSourceEventTime(),
                entity.getSourceDataVersion(),
                entity.getIndexValue(),
                entity.getClassification(),
                entity.getTimeUntilUpdateSeconds(),
                comparisonFacts,
                windowSummaries,
                List.of()
        );
    }

    public AnalysisOnchainContext toOnchainContext(
            OnchainFactSnapshotEntity entity,
            List<AnalysisOnchainComparisonFact> comparisonFacts,
            List<AnalysisOnchainWindowSummary> windowSummaries
    ) {
        return new AnalysisOnchainContext(
                entity.getSnapshotTime(),
                entity.getActiveAddressSourceEventTime(),
                entity.getTransactionCountSourceEventTime(),
                entity.getMarketCapSourceEventTime(),
                entity.getSourceDataVersion(),
                entity.getActiveAddressCount(),
                entity.getTransactionCount(),
                entity.getMarketCapUsd(),
                comparisonFacts,
                windowSummaries,
                List.of()
        );
    }

    public AnalysisMacroContext toMacroContext(
            MacroContextSnapshotEntity entity,
            List<AnalysisMacroComparisonFact> comparisonFacts,
            List<AnalysisMacroWindowSummary> windowSummaries
    ) {
        return new AnalysisMacroContext(
                entity.getSnapshotTime(),
                entity.getSourceDataVersion(),
                entity.getDxyObservationDate(),
                entity.getUs10yYieldObservationDate(),
                entity.getUsdKrwObservationDate(),
                entity.getDxyProxyValue(),
                entity.getUs10yYieldValue(),
                entity.getUsdKrwValue(),
                comparisonFacts,
                windowSummaries,
                List.of()
        );
    }

    public AnalysisSentimentWindowSummary toSentimentWindowSummary(SentimentWindowSummarySnapshotEntity entity) {
        return new AnalysisSentimentWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getAverageIndexValue(),
                entity.getCurrentIndexVsAverage(),
                entity.getGreedSampleCount(),
                entity.getFearSampleCount()
        );
    }

    public AnalysisMacroWindowSummary toMacroWindowSummary(MacroContextWindowSummarySnapshotEntity entity) {
        return new AnalysisMacroWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getAverageDxyProxyValue(),
                entity.getCurrentDxyProxyVsAverage(),
                entity.getAverageUs10yYieldValue(),
                entity.getCurrentUs10yYieldVsAverage(),
                entity.getAverageUsdKrwValue(),
                entity.getCurrentUsdKrwVsAverage()
        );
    }

    public AnalysisOnchainWindowSummary toOnchainWindowSummary(OnchainWindowSummarySnapshotEntity entity) {
        return new AnalysisOnchainWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getAverageActiveAddressCount(),
                entity.getCurrentActiveAddressVsAverage(),
                entity.getAverageTransactionCount(),
                entity.getCurrentTransactionCountVsAverage(),
                entity.getAverageMarketCapUsd(),
                entity.getCurrentMarketCapVsAverage()
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

    public AnalysisExternalContextCompositePayload toExternalContextComposite(MarketExternalContextSnapshotEntity entity) {
        return toExternalContextComposite(entity, List.of(), List.of(), List.of());
    }

    public AnalysisExternalContextCompositePayload toExternalContextComposite(
            MarketExternalContextSnapshotEntity entity,
            List<AnalysisExternalContextComparisonFact> comparisonFacts,
            List<AnalysisExternalContextHighlight> highlights
    ) {
        return toExternalContextComposite(entity, comparisonFacts, highlights, List.of(), List.of(), null, null);
    }

    public AnalysisExternalContextCompositePayload toExternalContextComposite(
            MarketExternalContextSnapshotEntity entity,
            List<AnalysisExternalContextComparisonFact> comparisonFacts,
            List<AnalysisExternalContextHighlight> highlights,
            List<AnalysisExternalContextWindowSummary> windowSummaries
    ) {
        return toExternalContextComposite(entity, comparisonFacts, highlights, windowSummaries, List.of(), null, null);
    }

    public AnalysisExternalContextCompositePayload toExternalContextComposite(
            MarketExternalContextSnapshotEntity entity,
            List<AnalysisExternalContextComparisonFact> comparisonFacts,
            List<AnalysisExternalContextHighlight> highlights,
            List<AnalysisExternalContextWindowSummary> windowSummaries,
            List<AnalysisExternalRegimeTransition> transitions,
            AnalysisExternalRegimePersistence persistence,
            AnalysisExternalRegimeStatePayload state
    ) {
        return new AnalysisExternalContextCompositePayload(
                entity.getSnapshotTime(),
                entity.getSourceDataVersion(),
                entity.getCompositeRiskScore(),
                enumValue(entity.getDominantDirection(), com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection.class),
                enumValue(entity.getHighestSeverity(), com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity.class),
                entity.getSupportiveSignalCount(),
                entity.getCautionarySignalCount(),
                entity.getHeadwindSignalCount(),
                enumValue(entity.getPrimarySignalCategory(), com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory.class),
                entity.getPrimarySignalTitle(),
                entity.getPrimarySignalDetail(),
                externalRegimeSignals(entity),
                comparisonFacts,
                highlights,
                windowSummaries,
                transitions,
                persistence,
                state
        );
    }

    public AnalysisExternalContextWindowSummary toExternalContextWindowSummary(
            MarketExternalContextWindowSummarySnapshotEntity entity
    ) {
        return new AnalysisExternalContextWindowSummary(
                MarketWindowType.valueOf(entity.getWindowType()),
                entity.getWindowStartTime(),
                entity.getWindowEndTime(),
                entity.getSampleCount(),
                entity.getAverageCompositeRiskScore(),
                entity.getCurrentCompositeRiskVsAverage(),
                entity.getSupportiveDominanceSampleCount(),
                entity.getCautionaryDominanceSampleCount(),
                entity.getHeadwindDominanceSampleCount(),
                entity.getHighSeveritySampleCount()
        );
    }

    public List<AnalysisExternalRegimeSignal> externalRegimeSignals(MarketExternalContextSnapshotEntity entity) {
        try {
            return objectMapper.readValue(
                    entity.getRegimeSignalsPayload(),
                    new TypeReference<List<AnalysisExternalRegimeSignal>>() {}
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize external context regime signals.", exception);
        }
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
                textLocalizationSupport.localizeSentence(entity.getRationale()),
                localizeSentences(stringListPayload(entity.getTriggerFactsPayload(), "Failed to deserialize market candidate level trigger facts."))
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
                localizeSentences(stringListPayload(entity.getTriggerFactsPayload(), "Failed to deserialize market candidate level zone trigger facts."))
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
            String supportLabel = formattingSupport.zoneLabel(nearestSupportZone.zoneLow(), nearestSupportZone.zoneHigh());
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.SUPPORT,
                    entity.getSupportZoneRank(),
                    AnalysisPriceZoneInteractionType.valueOf(entity.getSupportInteractionType()),
                    "가까운 지지 구간 %s은 현재 %s 상태이며 테스트는 %d회, 이탈 위험은 %s입니다."
                            .formatted(
                                    supportLabel,
                                    formattingSupport.interactionLabel(AnalysisPriceZoneInteractionType.valueOf(entity.getSupportInteractionType())),
                                    zeroSafe(entity.getSupportRecentTestCount()),
                                    formattingSupport.percentage(entity.getSupportBreakRisk())
                            ),
                    nearestSupportZone.triggerFacts()
            ));
        }
        if (nearestResistanceZone != null && entity.getResistanceInteractionType() != null) {
            String resistanceLabel = formattingSupport.zoneLabel(nearestResistanceZone.zoneLow(), nearestResistanceZone.zoneHigh());
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.RESISTANCE,
                    entity.getResistanceZoneRank(),
                    AnalysisPriceZoneInteractionType.valueOf(entity.getResistanceInteractionType()),
                    "가까운 저항 구간 %s은 현재 %s 상태이며 테스트는 %d회, 돌파 위험은 %s입니다."
                            .formatted(
                                    resistanceLabel,
                                    formattingSupport.interactionLabel(AnalysisPriceZoneInteractionType.valueOf(entity.getResistanceInteractionType())),
                                    zeroSafe(entity.getResistanceRecentTestCount()),
                                    formattingSupport.percentage(entity.getResistanceBreakRisk())
                            ),
                    nearestResistanceZone.triggerFacts()
            ));
        }
        return facts;
    }

    private int zeroSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private List<String> localizeSentences(List<String> values) {
        return values.stream()
                     .map(textLocalizationSupport::localizeSentence)
                     .toList();
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

    private <T extends Enum<T>> T enumValue(String value, Class<T> enumClass) {
        return value == null ? null : Enum.valueOf(enumClass, value);
    }
}

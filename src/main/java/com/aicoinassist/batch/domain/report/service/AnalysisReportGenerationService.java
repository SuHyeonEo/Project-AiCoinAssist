package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
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
        List<AnalysisPriceLevel> supportLevels = candidateLevels(candidateLevelSnapshots, "SUPPORT", Comparator.comparing(AnalysisPriceLevel::price).reversed());
        List<AnalysisPriceLevel> resistanceLevels = candidateLevels(candidateLevelSnapshots, "RESISTANCE", Comparator.comparing(AnalysisPriceLevel::price));
        AnalysisReportPayload payload = analysisReportAssembler.assemble(
                snapshot,
                reportType,
                comparisonFacts,
                windowSummaries,
                derivativeContext,
                continuityNotes,
                supportLevels,
                resistanceLevels
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

    private List<String> triggerFacts(String triggerFactsPayload) {
        try {
            return objectMapper.readValue(triggerFactsPayload, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize market candidate level trigger facts.", exception);
        }
    }
}

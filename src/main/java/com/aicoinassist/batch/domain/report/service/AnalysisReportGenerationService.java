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
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.service.MacroContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.service.SentimentSnapshotPersistenceService;
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
    private final MacroContextSnapshotPersistenceService macroContextSnapshotPersistenceService;
    private final SentimentSnapshotPersistenceService sentimentSnapshotPersistenceService;
    private final AnalysisComparisonService analysisComparisonService;
    private final AnalysisLevelContextComparisonService analysisLevelContextComparisonService;
    private final AnalysisDerivativeComparisonService analysisDerivativeComparisonService;
    private final AnalysisMacroComparisonService analysisMacroComparisonService;
    private final AnalysisSentimentComparisonService analysisSentimentComparisonService;
    private final AnalysisReportContinuityService analysisReportContinuityService;
    private final AnalysisReportAssembler analysisReportAssembler;
    private final AnalysisReportPersistenceService analysisReportPersistenceService;
    private final AnalysisReportMarketDataMapper analysisReportMarketDataMapper;

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
        MacroContextSnapshotEntity macroContextSnapshot = macroContextSnapshotPersistenceService.createAndSave();
        AnalysisMacroContext macroContext = analysisReportMarketDataMapper.toMacroContext(
                macroContextSnapshot,
                analysisMacroComparisonService.buildFacts(macroContextSnapshot, reportType)
        );
        SentimentSnapshotEntity sentimentSnapshot = sentimentSnapshotPersistenceService.createAndSaveFearGreedSnapshot();
        AnalysisSentimentContext sentimentContext = analysisReportMarketDataMapper.toSentimentContext(
                sentimentSnapshot,
                analysisSentimentComparisonService.buildFacts(sentimentSnapshot, reportType)
        );
        List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries = marketContextWindowSummarySnapshotPersistenceService
                .createAndSaveForReportType(marketContextSnapshot, reportType)
                .stream()
                .map(analysisReportMarketDataMapper::toDerivativeWindowSummary)
                .toList();
        AnalysisDerivativeContext derivativeContext = analysisReportMarketDataMapper.toDerivativeContext(
                marketContextSnapshot,
                derivativeComparisonFacts,
                derivativeWindowSummaries
        );
        List<AnalysisWindowSummary> windowSummaries = marketWindowSummarySnapshotPersistenceService
                .createAndSaveForReportType(snapshot, reportType)
                .stream()
                .map(analysisReportMarketDataMapper::toWindowSummary)
                .toList();
        List<MarketCandidateLevelSnapshotEntity> candidateLevelSnapshots = marketCandidateLevelSnapshotPersistenceService
                .createAndSaveAll(snapshot);
        List<MarketCandidateLevelZoneSnapshotEntity> candidateLevelZoneSnapshots = marketCandidateLevelZoneSnapshotPersistenceService
                .createAndSaveAll(candidateLevelSnapshots);
        List<AnalysisPriceLevel> supportLevels = analysisReportMarketDataMapper.toCandidateLevels(
                candidateLevelSnapshots,
                "SUPPORT",
                Comparator.comparing(AnalysisPriceLevel::price).reversed()
        );
        List<AnalysisPriceLevel> resistanceLevels = analysisReportMarketDataMapper.toCandidateLevels(
                candidateLevelSnapshots,
                "RESISTANCE",
                Comparator.comparing(AnalysisPriceLevel::price)
        );
        List<AnalysisPriceZone> supportZones = analysisReportMarketDataMapper.toCandidateZones(candidateLevelZoneSnapshots, "SUPPORT");
        List<AnalysisPriceZone> resistanceZones = analysisReportMarketDataMapper.toCandidateZones(candidateLevelZoneSnapshots, "RESISTANCE");
        MarketLevelContextSnapshotEntity levelContextSnapshot = marketLevelContextSnapshotPersistenceService
                .createAndSave(snapshot, candidateLevelZoneSnapshots);
        List<AnalysisLevelContextComparisonFact> levelContextComparisonFacts = analysisLevelContextComparisonService
                .buildFacts(levelContextSnapshot, reportType);
        AnalysisLevelContextPayload levelContext = analysisReportMarketDataMapper.toLevelContext(
                levelContextSnapshot,
                supportZones,
                resistanceZones,
                levelContextComparisonFacts
        );
        AnalysisReportPayload payload = analysisReportAssembler.assemble(
                snapshot,
                reportType,
                comparisonFacts,
                windowSummaries,
                derivativeContext,
                macroContext,
                sentimentContext,
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
}

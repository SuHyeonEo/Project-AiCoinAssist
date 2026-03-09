package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportGenerationService {

    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;
    private final MarketWindowSummarySnapshotPersistenceService marketWindowSummarySnapshotPersistenceService;
    private final AnalysisComparisonService analysisComparisonService;
    private final AnalysisReportAssembler analysisReportAssembler;
    private final AnalysisReportPersistenceService analysisReportPersistenceService;

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
        List<AnalysisWindowSummary> windowSummaries = marketWindowSummarySnapshotPersistenceService
                .createAndSaveForReportType(snapshot, reportType)
                .stream()
                .map(this::toWindowSummary)
                .toList();
        AnalysisReportPayload payload = analysisReportAssembler.assemble(snapshot, reportType, comparisonFacts, windowSummaries);
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
}

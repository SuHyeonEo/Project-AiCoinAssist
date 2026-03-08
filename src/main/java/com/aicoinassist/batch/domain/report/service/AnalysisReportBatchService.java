package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchService {

    private final MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;
    private final AnalysisReportGenerationService analysisReportGenerationService;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;
    private final Clock clock;

    public AnalysisReportBatchResult generateForAsset(
            AssetType assetType,
            String runId,
            String engineVersion,
            Instant storedTime
    ) {
        Instant startedAt = Instant.now(clock);
        String symbol = assetType.symbol();
        List<AnalysisReportSnapshotStepResult> snapshotResults = new ArrayList<>();
        for (CandleInterval interval : analysisReportBatchProperties.snapshotIntervals()) {
            try {
                marketIndicatorSnapshotPersistenceService.createAndSave(symbol, interval);
                snapshotResults.add(new AnalysisReportSnapshotStepResult(interval, true, null));
            } catch (Exception exception) {
                snapshotResults.add(new AnalysisReportSnapshotStepResult(interval, false, exception.getMessage()));
            }
        }

        List<AnalysisReportStepResult> reportResults = new ArrayList<>();
        for (var reportType : analysisReportBatchProperties.reportTypes()) {
            try {
                analysisReportGenerationService.generateAndSave(
                        symbol,
                        reportType,
                        engineVersion,
                        storedTime
                );
                reportResults.add(new AnalysisReportStepResult(reportType, true, null));
            } catch (Exception exception) {
                reportResults.add(new AnalysisReportStepResult(reportType, false, exception.getMessage()));
            }
        }

        Instant finishedAt = Instant.now(clock);
        return new AnalysisReportBatchResult(
                runId,
                symbol,
                startedAt,
                finishedAt,
                finishedAt.toEpochMilli() - startedAt.toEpochMilli(),
                snapshotResults,
                reportResults,
                null
        );
    }
}

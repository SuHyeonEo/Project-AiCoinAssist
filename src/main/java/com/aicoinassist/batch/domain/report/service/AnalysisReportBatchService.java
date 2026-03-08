package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchService {

    private final MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;
    private final AnalysisReportGenerationService analysisReportGenerationService;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;

    public AnalysisReportBatchResult generateForAsset(
            AssetType assetType,
            Instant storedTime
    ) {
        String symbol = assetType.symbol();
        int snapshotCount = 0;
        for (CandleInterval interval : analysisReportBatchProperties.snapshotIntervals()) {
            marketIndicatorSnapshotPersistenceService.createAndSave(symbol, interval);
            snapshotCount++;
        }

        int reportCount = 0;
        for (var reportType : analysisReportBatchProperties.reportTypes()) {
            analysisReportGenerationService.generateAndSave(
                    symbol,
                    reportType,
                    analysisReportBatchProperties.engineVersion(),
                    storedTime
            );
            reportCount++;
        }

        return new AnalysisReportBatchResult(symbol, snapshotCount, reportCount);
    }
}

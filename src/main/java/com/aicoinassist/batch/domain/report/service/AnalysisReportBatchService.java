package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchService {

    private final MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;
    private final AnalysisReportGenerationService analysisReportGenerationService;

    public AnalysisReportBatchResult generateForSymbol(
            String symbol,
            String analysisEngineVersion,
            Instant storedTime
    ) {
        int snapshotCount = 0;
        for (CandleInterval interval : CandleInterval.values()) {
            marketIndicatorSnapshotPersistenceService.createAndSave(symbol, interval);
            snapshotCount++;
        }

        int reportCount = 0;
        for (AnalysisReportType reportType : AnalysisReportType.values()) {
            analysisReportGenerationService.generateAndSave(symbol, reportType, analysisEngineVersion, storedTime);
            reportCount++;
        }

        return new AnalysisReportBatchResult(symbol, snapshotCount, reportCount);
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
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
    private final AnalysisReportNarrativeGenerationFlowService analysisReportNarrativeGenerationFlowService;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;
    private final AnalysisLlmNarrativeProperties analysisLlmNarrativeProperties;
    private final Clock clock;

    public AnalysisReportBatchResult generateForAsset(
            AssetType assetType,
            String runId,
            String engineVersion,
            Instant storedTime,
            List<com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType> targetReportTypes
    ) {
        Instant startedAt = Instant.now(clock);
        String symbol = assetType.symbol();
        List<AnalysisReportSnapshotStepResult> snapshotResults = new ArrayList<>();
        for (CandleInterval interval : analysisReportBatchProperties.snapshotIntervals(targetReportTypes)) {
            try {
                marketIndicatorSnapshotPersistenceService.createAndSave(symbol, interval);
                snapshotResults.add(new AnalysisReportSnapshotStepResult(interval, true, null));
            } catch (Exception exception) {
                snapshotResults.add(new AnalysisReportSnapshotStepResult(interval, false, exception.getMessage()));
            }
        }

        List<AnalysisReportStepResult> reportResults = new ArrayList<>();
        for (var reportType : targetReportTypes) {
            try {
                analysisReportGenerationService.generateAndSave(
                        symbol,
                        reportType,
                        engineVersion,
                        storedTime
                );
                reportResults.add(generateNarrativeResult(symbol, reportType));
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

    private AnalysisReportStepResult generateNarrativeResult(
            String symbol,
            com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType reportType
    ) {
        if (!analysisLlmNarrativeProperties.enabled()) {
            return new AnalysisReportStepResult(reportType, true, null);
        }

        try {
            AnalysisReportNarrativeEntity narrative = analysisReportNarrativeGenerationFlowService
                    .generateAndStoreLatest(symbol, reportType);
            return new AnalysisReportStepResult(
                    reportType,
                    true,
                    null,
                    narrative.getGenerationStatus(),
                    narrative.isFallbackUsed(),
                    narrative.getFailureType(),
                    null
            );
        } catch (Exception exception) {
            return new AnalysisReportStepResult(
                    reportType,
                    true,
                    null,
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus.FAILED,
                    false,
                    AnalysisLlmNarrativeFailureType.UNKNOWN,
                    exception.getMessage()
            );
        }
    }
}

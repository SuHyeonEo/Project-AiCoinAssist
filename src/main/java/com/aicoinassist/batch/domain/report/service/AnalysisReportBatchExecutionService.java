package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchExecutionService {

    private final AnalysisReportBatchService analysisReportBatchService;
    private final AnalysisReportBatchRunPersistenceService analysisReportBatchRunPersistenceService;
    private final Clock clock;

    public AnalysisReportBatchRunResult execute(
            List<AssetType> assetTypes,
            String engineVersion,
            BatchExecutionTriggerType triggerType,
            String rerunSourceRunId
    ) {
        String runId = UUID.randomUUID().toString();
        Instant runStartedAt = Instant.now(clock);
        Instant storedTime = runStartedAt;
        List<AnalysisReportBatchResult> assetResults = new ArrayList<>();

        for (AssetType assetType : assetTypes) {
            Instant assetStartedAt = Instant.now(clock);
            try {
                assetResults.add(
                        analysisReportBatchService.generateForAsset(
                                assetType,
                                runId,
                                engineVersion,
                                storedTime
                        )
                );
            } catch (Exception exception) {
                assetResults.add(
                        AnalysisReportBatchResult.crashed(
                                runId,
                                assetType.symbol(),
                                assetStartedAt,
                                Instant.now(clock),
                                crashMessage(exception)
                        )
                );
            }
        }

        Instant runFinishedAt = Instant.now(clock);
        AnalysisReportBatchRunResult runResult = new AnalysisReportBatchRunResult(
                runId,
                triggerType,
                rerunSourceRunId,
                runStartedAt,
                runFinishedAt,
                runFinishedAt.toEpochMilli() - runStartedAt.toEpochMilli(),
                List.copyOf(assetResults)
        );
        analysisReportBatchRunPersistenceService.save(runResult, engineVersion, runFinishedAt);
        return runResult;
    }

    private static String crashMessage(Exception exception) {
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }
}

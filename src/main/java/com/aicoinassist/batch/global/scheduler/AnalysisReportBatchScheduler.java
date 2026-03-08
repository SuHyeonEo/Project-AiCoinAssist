package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.scheduler.analysis-report-generation",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AnalysisReportBatchScheduler {

    private final AnalysisReportBatchExecutionService analysisReportBatchExecutionService;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;

    @Scheduled(fixedDelayString = "${batch.analysis-report.fixed-delay-ms:300000}")
    public void run() {
        AnalysisReportBatchRunResult runResult = analysisReportBatchExecutionService.execute(
                analysisReportBatchProperties.assetTypes(),
                analysisReportBatchProperties.engineVersion(),
                BatchExecutionTriggerType.SCHEDULED,
                null
        );
        for (var result : runResult.assetResults()) {
            if (result.hasFailures()) {
                log.warn(
                        "analysis report batch partially failed - runId: {}, symbol: {}, durationMs: {}, snapshotSuccess: {}, snapshotFailure: {}, reportSuccess: {}, reportFailure: {}, engineVersion: {}, triggerType: {}",
                        result.runId(),
                        result.symbol(),
                        result.durationMillis(),
                        result.snapshotSuccessCount(),
                        result.snapshotFailureCount(),
                        result.reportSuccessCount(),
                        result.reportFailureCount(),
                        analysisReportBatchProperties.engineVersion(),
                        runResult.triggerType()
                );
                log.warn("snapshot step results - runId: {}, symbol: {}, results: {}", result.runId(), result.symbol(), result.snapshotResults());
                log.warn("report step results - runId: {}, symbol: {}, results: {}", result.runId(), result.symbol(), result.reportResults());
                if (result.crashed()) {
                    log.error(
                            "analysis report batch crashed - runId: {}, symbol: {}, durationMs: {}, engineVersion: {}, triggerType: {}, error: {}",
                            result.runId(),
                            result.symbol(),
                            result.durationMillis(),
                            analysisReportBatchProperties.engineVersion(),
                            runResult.triggerType(),
                            result.crashErrorMessage()
                    );
                }
            } else {
                log.info(
                        "analysis report batch completed - runId: {}, symbol: {}, durationMs: {}, snapshots: {}, reports: {}, engineVersion: {}, triggerType: {}",
                        result.runId(),
                        result.symbol(),
                        result.durationMillis(),
                        result.snapshotSuccessCount(),
                        result.reportSuccessCount(),
                        analysisReportBatchProperties.engineVersion(),
                        runResult.triggerType()
                );
            }
        }
        log.info(
                "analysis report batch run finished - runId: {}, triggerType: {}, rerunSourceRunId: {}, status: {}, durationMs: {}, assetSuccess: {}, assetFailure: {}, engineVersion: {}",
                runResult.runId(),
                runResult.triggerType(),
                runResult.rerunSourceRunId(),
                runResult.status(),
                runResult.durationMillis(),
                runResult.assetSuccessCount(),
                runResult.assetFailureCount(),
                analysisReportBatchProperties.engineVersion()
        );
    }
}

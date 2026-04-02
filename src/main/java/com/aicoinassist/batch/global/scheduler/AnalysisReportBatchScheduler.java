package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
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

    @Scheduled(
            cron = "${batch.analysis-report.short-term-cron:0 0 0/6 * * *}",
            zone = "${batch.analysis-report.zone:Asia/Seoul}"
    )
    public void runShortTerm() {
        runForReportType(AnalysisReportType.SHORT_TERM);
    }

    @Scheduled(
            cron = "${batch.analysis-report.mid-term-cron:0 0 0 * * *}",
            zone = "${batch.analysis-report.zone:Asia/Seoul}"
    )
    public void runMidTerm() {
        runForReportType(AnalysisReportType.MID_TERM);
    }

    @Scheduled(
            cron = "${batch.analysis-report.long-term-cron:0 0 0 * * MON}",
            zone = "${batch.analysis-report.zone:Asia/Seoul}"
    )
    public void runLongTerm() {
        runForReportType(AnalysisReportType.LONG_TERM);
    }

    private void runForReportType(AnalysisReportType reportType) {
        if (!analysisReportBatchProperties.isEnabled(reportType)) {
            return;
        }

        AnalysisReportBatchRunResult runResult = analysisReportBatchExecutionService.execute(
                analysisReportBatchProperties.assetTypes(),
                List.of(reportType),
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
                log.warn("snapshot step results - runId: {}, symbol: {}, reportType: {}, results: {}", result.runId(), result.symbol(), reportType, result.snapshotResults());
                log.warn("report step results - runId: {}, symbol: {}, reportType: {}, results: {}", result.runId(), result.symbol(), reportType, result.reportResults());
                if (result.crashed()) {
                    log.error(
                            "analysis report batch crashed - runId: {}, symbol: {}, reportType: {}, durationMs: {}, engineVersion: {}, triggerType: {}, error: {}",
                            result.runId(),
                            result.symbol(),
                            reportType,
                            result.durationMillis(),
                            analysisReportBatchProperties.engineVersion(),
                            runResult.triggerType(),
                            result.crashErrorMessage()
                    );
                }
            } else {
                log.info(
                        "analysis report batch completed - runId: {}, symbol: {}, reportType: {}, durationMs: {}, snapshots: {}, reports: {}, engineVersion: {}, triggerType: {}",
                        result.runId(),
                        result.symbol(),
                        reportType,
                        result.durationMillis(),
                        result.snapshotSuccessCount(),
                        result.reportSuccessCount(),
                        analysisReportBatchProperties.engineVersion(),
                        runResult.triggerType()
                );
            }
        }
        log.info(
                "analysis report batch run finished - runId: {}, reportType: {}, triggerType: {}, rerunSourceRunId: {}, status: {}, durationMs: {}, assetSuccess: {}, assetFailure: {}, engineVersion: {}",
                runResult.runId(),
                reportType,
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

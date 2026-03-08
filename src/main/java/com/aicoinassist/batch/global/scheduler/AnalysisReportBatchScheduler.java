package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final AnalysisReportBatchService analysisReportBatchService;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${batch.analysis-report.fixed-delay-ms:300000}")
    public void run() {
        String runId = UUID.randomUUID().toString();
        Instant runStartedAt = Instant.now(clock);
        Instant storedTime = runStartedAt;
        List<AnalysisReportBatchResult> assetResults = new ArrayList<>();

        for (AssetType assetType : analysisReportBatchProperties.assetTypes()) {
            try {
                AnalysisReportBatchResult result = analysisReportBatchService.generateForAsset(
                        assetType,
                        runId,
                        storedTime
                );
                assetResults.add(result);

                if (result.hasFailures()) {
                    log.warn(
                            "analysis report batch partially failed - runId: {}, symbol: {}, durationMs: {}, snapshotSuccess: {}, snapshotFailure: {}, reportSuccess: {}, reportFailure: {}, engineVersion: {}",
                            result.runId(),
                            result.symbol(),
                            result.durationMillis(),
                            result.snapshotSuccessCount(),
                            result.snapshotFailureCount(),
                            result.reportSuccessCount(),
                            result.reportFailureCount(),
                            analysisReportBatchProperties.engineVersion()
                    );
                    log.warn("snapshot step results - runId: {}, symbol: {}, results: {}", result.runId(), result.symbol(), result.snapshotResults());
                    log.warn("report step results - runId: {}, symbol: {}, results: {}", result.runId(), result.symbol(), result.reportResults());
                } else {
                    log.info(
                            "analysis report batch completed - runId: {}, symbol: {}, durationMs: {}, snapshots: {}, reports: {}, engineVersion: {}",
                            result.runId(),
                            result.symbol(),
                            result.durationMillis(),
                            result.snapshotSuccessCount(),
                            result.reportSuccessCount(),
                            analysisReportBatchProperties.engineVersion()
                    );
                }
            } catch (Exception exception) {
                log.error(
                        "analysis report batch crashed - runId: {}, symbol: {}, engineVersion: {}",
                        runId,
                        assetType.symbol(),
                        analysisReportBatchProperties.engineVersion(),
                        exception
                );
            }
        }

        Instant runFinishedAt = Instant.now(clock);
        AnalysisReportBatchRunResult runResult = new AnalysisReportBatchRunResult(
                runId,
                runStartedAt,
                runFinishedAt,
                runFinishedAt.toEpochMilli() - runStartedAt.toEpochMilli(),
                List.copyOf(assetResults)
        );
        log.info(
                "analysis report batch run finished - runId: {}, durationMs: {}, assetSuccess: {}, assetFailure: {}, engineVersion: {}",
                runResult.runId(),
                runResult.durationMillis(),
                runResult.assetSuccessCount(),
                runResult.assetFailureCount(),
                analysisReportBatchProperties.engineVersion()
        );
    }
}

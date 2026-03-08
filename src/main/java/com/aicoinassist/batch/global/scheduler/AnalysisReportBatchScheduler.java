package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

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

    @Scheduled(fixedDelayString = "${batch.analysis-report.fixed-delay-ms:300000}")
    public void run() {
        Instant storedTime = Instant.now();

        for (AssetType assetType : analysisReportBatchProperties.assetTypes()) {
            try {
                AnalysisReportBatchResult result = analysisReportBatchService.generateForAsset(
                        assetType,
                        storedTime
                );

                if (result.hasFailures()) {
                    log.warn(
                            "analysis report batch partially failed - symbol: {}, snapshotSuccess: {}, snapshotFailure: {}, reportSuccess: {}, reportFailure: {}, engineVersion: {}",
                            result.symbol(),
                            result.snapshotSuccessCount(),
                            result.snapshotFailureCount(),
                            result.reportSuccessCount(),
                            result.reportFailureCount(),
                            analysisReportBatchProperties.engineVersion()
                    );
                    log.warn("snapshot step results - symbol: {}, results: {}", result.symbol(), result.snapshotResults());
                    log.warn("report step results - symbol: {}, results: {}", result.symbol(), result.reportResults());
                } else {
                    log.info(
                            "analysis report batch completed - symbol: {}, snapshots: {}, reports: {}, engineVersion: {}",
                            result.symbol(),
                            result.snapshotSuccessCount(),
                            result.reportSuccessCount(),
                            analysisReportBatchProperties.engineVersion()
                    );
                }
            } catch (Exception exception) {
                log.error(
                        "analysis report batch crashed - symbol: {}, engineVersion: {}",
                        assetType.symbol(),
                        analysisReportBatchProperties.engineVersion(),
                        exception
                );
            }
        }
    }
}

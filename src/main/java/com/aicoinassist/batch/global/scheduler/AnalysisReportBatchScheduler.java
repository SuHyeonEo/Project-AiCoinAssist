package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.service.AnalysisReportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${batch.analysis-report.engine-version:report-assembler-v1}")
    private String analysisEngineVersion;

    @Scheduled(fixedDelayString = "${batch.scheduler.analysis-report-generation.fixed-delay-ms:300000}")
    public void run() {
        Instant storedTime = Instant.now();

        for (AssetType assetType : AssetType.values()) {
            try {
                AnalysisReportBatchResult result = analysisReportBatchService.generateForSymbol(
                        assetType.symbol(),
                        analysisEngineVersion,
                        storedTime
                );

                log.info(
                        "analysis report batch completed - symbol: {}, snapshots: {}, reports: {}, engineVersion: {}",
                        result.symbol(),
                        result.snapshotCount(),
                        result.reportCount(),
                        analysisEngineVersion
                );
            } catch (Exception exception) {
                log.error(
                        "analysis report batch failed - symbol: {}, engineVersion: {}",
                        assetType.symbol(),
                        analysisEngineVersion,
                        exception
                );
            }
        }
    }
}

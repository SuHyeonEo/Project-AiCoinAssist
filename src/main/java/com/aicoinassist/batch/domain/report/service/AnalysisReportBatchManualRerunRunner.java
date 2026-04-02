package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchManualRerunProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.analysis-report.manual-rerun",
        name = "enabled",
        havingValue = "true"
)
public class AnalysisReportBatchManualRerunRunner implements ApplicationRunner {

    private final AnalysisReportBatchManualRerunProperties manualRerunProperties;
    private final AnalysisReportBatchRerunService analysisReportBatchRerunService;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        AnalysisReportBatchRunResult rerunResult = analysisReportBatchRerunService
                .rerunFailedAssets(manualRerunProperties.sourceRunId());

        log.info(
                "manual analysis report rerun finished - sourceRunId: {}, rerunRunId: {}, status: {}, assetSuccess: {}, assetFailure: {}",
                manualRerunProperties.sourceRunId(),
                rerunResult.runId(),
                rerunResult.status(),
                rerunResult.assetSuccessCount(),
                rerunResult.assetFailureCount()
        );

        if (manualRerunProperties.shutdownAfterRun()) {
            log.info(
                    "manual analysis report rerun requested shutdown - sourceRunId: {}, rerunRunId: {}",
                    manualRerunProperties.sourceRunId(),
                    rerunResult.runId()
            );
            applicationContext.close();
        }
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisReportNarrativeManualGenerateProperties;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
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
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.llm-narrative.manual-generate",
        name = "enabled",
        havingValue = "true"
)
public class AnalysisReportNarrativeManualGenerateRunner implements ApplicationRunner {

    private final AnalysisReportNarrativeManualGenerateProperties properties;
    private final AnalysisReportNarrativeGenerationFlowService analysisReportNarrativeGenerationFlowService;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        AnalysisReportNarrativeEntity narrative = analysisReportNarrativeGenerationFlowService
                .generateAndStoreLatest(properties.symbol(), properties.reportType());

        log.info(
                "manual latest narrative generation finished - symbol: {}, reportType: {}, narrativeId: {}, status: {}, fallbackUsed: {}, failureType: {}",
                properties.symbol(),
                properties.reportType(),
                narrative.getId(),
                narrative.getGenerationStatus(),
                narrative.isFallbackUsed(),
                narrative.getFailureType()
        );

        if (properties.shutdownAfterRun()) {
            log.info(
                    "manual latest narrative generation requested shutdown - symbol: {}, reportType: {}, narrativeId: {}",
                    properties.symbol(),
                    properties.reportType(),
                    narrative.getId()
            );
            applicationContext.close();
        }
    }
}

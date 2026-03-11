package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsManualGenerateProperties;
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
@Order(2)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.reference-news.manual-generate",
        name = "enabled",
        havingValue = "true"
)
public class ReferenceNewsManualGenerateRunner implements ApplicationRunner {

    private final ReferenceNewsManualGenerateProperties properties;
    private final ReferenceNewsSnapshotGenerationFlowService referenceNewsSnapshotGenerationFlowService;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        ReferenceNewsSnapshotGenerationFlowResult result =
                referenceNewsSnapshotGenerationFlowService.generateTodayIfMissing();

        log.info(
                "manual reference news generation finished - created: {}, snapshotId: {}, scope: {}, snapshotDate: {}, articleCount: {}",
                result.created(),
                result.snapshot().getId(),
                result.snapshot().getScope(),
                result.snapshot().getSnapshotDate(),
                result.snapshot().getArticleCount()
        );

        if (properties.shutdownAfterRun()) {
            log.info(
                    "manual reference news generation requested shutdown - snapshotId: {}, snapshotDate: {}",
                    result.snapshot().getId(),
                    result.snapshot().getSnapshotDate()
            );
            applicationContext.close();
        }
    }
}

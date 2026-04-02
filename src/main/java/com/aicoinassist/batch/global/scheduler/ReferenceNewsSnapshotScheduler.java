package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.news.service.ReferenceNewsSnapshotGenerationFlowResult;
import com.aicoinassist.batch.domain.news.service.ReferenceNewsSnapshotGenerationFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.reference-news",
        name = "enabled",
        havingValue = "true"
)
public class ReferenceNewsSnapshotScheduler {

    private final ReferenceNewsSnapshotGenerationFlowService referenceNewsSnapshotGenerationFlowService;

    @Scheduled(
            fixedDelayString = "${batch.reference-news.daily-fixed-delay-ms:86400000}",
            initialDelayString = "${batch.reference-news.daily-initial-delay-ms:240000}"
    )
    public void runDaily() {
        ReferenceNewsSnapshotGenerationFlowResult result =
                referenceNewsSnapshotGenerationFlowService.generateTodayIfMissing();

        if (result.created()) {
            log.info(
                    "reference news snapshot created - snapshotId: {}, scope: {}, snapshotDate: {}, articleCount: {}",
                    result.snapshot().getId(),
                    result.snapshot().getScope(),
                    result.snapshot().getSnapshotDate(),
                    result.snapshot().getArticleCount()
            );
            return;
        }

        log.info(
                "reference news snapshot skipped because it already exists - snapshotId: {}, scope: {}, snapshotDate: {}, articleCount: {}",
                result.snapshot().getId(),
                result.snapshot().getScope(),
                result.snapshot().getSnapshotDate(),
                result.snapshot().getArticleCount()
        );
    }
}

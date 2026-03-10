package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.sentiment.dto.FearGreedRawSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRawRepository;
import com.aicoinassist.batch.infrastructure.client.alternativeme.AlternativeMeSentimentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SentimentRawIngestionService {

    private static final String ALTERNATIVE_ME_SOURCE = "ALTERNATIVE_ME";

    private final AlternativeMeSentimentClient alternativeMeSentimentClient;
    private final SentimentSnapshotRawRepository sentimentSnapshotRawRepository;

    @Transactional
    public SentimentSnapshotRawEntity ingestFearGreed() {
        Instant collectedTime = Instant.now();
        FearGreedRawSnapshot snapshot = alternativeMeSentimentClient.fetchLatestFearGreed();

        SentimentSnapshotRawEntity existingEntity = snapshot.sourceEventTime() == null
                ? null
                : sentimentSnapshotRawRepository
                        .findTopBySourceAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                                ALTERNATIVE_ME_SOURCE,
                                SentimentMetricType.FEAR_GREED_INDEX,
                                snapshot.sourceEventTime()
                        )
                        .orElse(null);

        if (existingEntity == null) {
            SentimentSnapshotRawEntity entity = SentimentSnapshotRawEntity.builder()
                                                                          .source(ALTERNATIVE_ME_SOURCE)
                                                                          .metricType(snapshot.metricType())
                                                                          .sourceEventTime(snapshot.sourceEventTime())
                                                                          .collectedTime(collectedTime)
                                                                          .validationStatus(snapshot.validation().status())
                                                                          .validationDetails(snapshot.validation().details())
                                                                          .indexValue(snapshot.indexValue())
                                                                          .classification(snapshot.classification())
                                                                          .timeUntilUpdateSeconds(snapshot.timeUntilUpdateSeconds())
                                                                          .rawPayload(snapshot.rawPayload())
                                                                          .build();
            return sentimentSnapshotRawRepository.save(entity);
        }

        existingEntity.refreshFromIngestion(
                collectedTime,
                snapshot.validation().status(),
                snapshot.validation().details(),
                snapshot.indexValue(),
                snapshot.classification(),
                snapshot.timeUntilUpdateSeconds(),
                snapshot.rawPayload()
        );
        return existingEntity;
    }
}

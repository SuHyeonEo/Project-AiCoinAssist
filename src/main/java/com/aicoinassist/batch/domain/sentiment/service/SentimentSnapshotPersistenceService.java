package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.sentiment.dto.SentimentSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SentimentSnapshotPersistenceService {

    private final SentimentSnapshotService sentimentSnapshotService;
    private final SentimentSnapshotRepository sentimentSnapshotRepository;

    @Transactional
    public SentimentSnapshotEntity createAndSaveFearGreedSnapshot() {
        SentimentSnapshot snapshot = sentimentSnapshotService.createFearGreedSnapshot();

        SentimentSnapshotEntity existingEntity = sentimentSnapshotRepository
                .findTopByMetricTypeAndSnapshotTimeOrderByIdDesc(
                        SentimentMetricType.FEAR_GREED_INDEX,
                        snapshot.snapshotTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            SentimentSnapshotEntity entity = SentimentSnapshotEntity.builder()
                                                                    .metricType(snapshot.metricType())
                                                                    .snapshotTime(snapshot.snapshotTime())
                                                                    .sourceEventTime(snapshot.sourceEventTime())
                                                                    .sourceDataVersion(snapshot.sourceDataVersion())
                                                                    .indexValue(snapshot.indexValue())
                                                                    .classification(snapshot.classification())
                                                                    .timeUntilUpdateSeconds(snapshot.timeUntilUpdateSeconds())
                                                                    .previousSnapshotTime(snapshot.previousSnapshotTime())
                                                                    .previousIndexValue(snapshot.previousIndexValue())
                                                                    .valueChange(snapshot.valueChange())
                                                                    .valueChangeRate(snapshot.valueChangeRate())
                                                                    .classificationChanged(snapshot.classificationChanged())
                                                                    .build();
            return sentimentSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.sourceEventTime(),
                snapshot.sourceDataVersion(),
                snapshot.indexValue(),
                snapshot.classification(),
                snapshot.timeUntilUpdateSeconds(),
                snapshot.previousSnapshotTime(),
                snapshot.previousIndexValue(),
                snapshot.valueChange(),
                snapshot.valueChangeRate(),
                snapshot.classificationChanged()
        );
        return existingEntity;
    }
}

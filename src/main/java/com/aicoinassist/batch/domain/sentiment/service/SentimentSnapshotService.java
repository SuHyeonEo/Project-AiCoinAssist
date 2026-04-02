package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.sentiment.dto.SentimentSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRawRepository;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SentimentSnapshotService {

    private final SentimentSnapshotRawRepository sentimentSnapshotRawRepository;
    private final SentimentSnapshotRepository sentimentSnapshotRepository;

    public SentimentSnapshot createFearGreedSnapshot() {
        SentimentSnapshotRawEntity latestRawSnapshot = sentimentSnapshotRawRepository
                .findTopByMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(SentimentMetricType.FEAR_GREED_INDEX)
                .orElseThrow(() -> new IllegalStateException("No Fear & Greed raw snapshot found."));

        if (latestRawSnapshot.getValidationStatus() != RawDataValidationStatus.VALID) {
            throw new IllegalStateException("Fear & Greed raw snapshot is invalid: " + latestRawSnapshot.getValidationDetails());
        }

        Instant snapshotTime = latestRawSnapshot.getSourceEventTime();
        SentimentSnapshotEntity previousSnapshot = sentimentSnapshotRepository
                .findTopByMetricTypeAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
                        SentimentMetricType.FEAR_GREED_INDEX,
                        snapshotTime
                )
                .orElse(null);

        BigDecimal previousIndexValue = previousSnapshot == null ? null : previousSnapshot.getIndexValue();
        BigDecimal valueChange = previousIndexValue == null
                ? null
                : latestRawSnapshot.getIndexValue().subtract(previousIndexValue);
        BigDecimal valueChangeRate = previousIndexValue == null || previousIndexValue.compareTo(BigDecimal.ZERO) == 0
                ? null
                : valueChange.divide(previousIndexValue, 8, RoundingMode.HALF_UP);
        Boolean classificationChanged = previousSnapshot == null
                ? null
                : !Objects.equals(latestRawSnapshot.getClassification(), previousSnapshot.getClassification());

        return new SentimentSnapshot(
                SentimentMetricType.FEAR_GREED_INDEX,
                snapshotTime,
                latestRawSnapshot.getSourceEventTime(),
                buildSourceDataVersion(latestRawSnapshot),
                latestRawSnapshot.getIndexValue(),
                latestRawSnapshot.getClassification(),
                latestRawSnapshot.getTimeUntilUpdateSeconds(),
                previousSnapshot == null ? null : previousSnapshot.getSnapshotTime(),
                previousIndexValue,
                valueChange,
                valueChangeRate,
                classificationChanged
        );
    }

    private String buildSourceDataVersion(SentimentSnapshotRawEntity latestRawSnapshot) {
        return "metricType=" + latestRawSnapshot.getMetricType()
                + ";sourceEventTime=" + latestRawSnapshot.getSourceEventTime();
    }
}

package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.sentiment.dto.SentimentWindowSummarySnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentimentWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final SentimentSnapshotRepository sentimentSnapshotRepository;

    public SentimentWindowSummarySnapshot create(
            SentimentSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<SentimentSnapshotEntity> snapshots = sentimentSnapshotRepository
                .findAllByMetricTypeAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        currentSnapshot.getMetricType(),
                        windowStartTime,
                        windowEndTime
                );

        if (snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No sentiment snapshots found for window summary: metric=%s window=%s end=%s"
                            .formatted(currentSnapshot.getMetricType(), windowType.name(), currentSnapshot.getSnapshotTime())
            );
        }

        BigDecimal averageIndexValue = average(snapshots.stream().map(SentimentSnapshotEntity::getIndexValue).toList());
        int greedSampleCount = (int) snapshots.stream().filter(snapshot -> isGreed(snapshot.getClassification())).count();
        int fearSampleCount = (int) snapshots.stream().filter(snapshot -> isFear(snapshot.getClassification())).count();

        return new SentimentWindowSummarySnapshot(
                currentSnapshot.getMetricType(),
                windowType,
                windowStartTime,
                windowEndTime,
                snapshots.size(),
                currentSnapshot.getIndexValue(),
                averageIndexValue,
                deltaRatio(currentSnapshot.getIndexValue(), averageIndexValue),
                currentSnapshot.getClassification(),
                greedSampleCount,
                fearSampleCount,
                buildSourceDataVersion(currentSnapshot, windowType, windowStartTime, windowEndTime, snapshots.size())
        );
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal deltaRatio(BigDecimal currentValue, BigDecimal averageValue) {
        if (averageValue == null || averageValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentValue.subtract(averageValue)
                           .divide(averageValue, RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isGreed(String classification) {
        return "Greed".equalsIgnoreCase(classification) || "Extreme Greed".equalsIgnoreCase(classification);
    }

    private boolean isFear(String classification) {
        return "Fear".equalsIgnoreCase(classification) || "Extreme Fear".equalsIgnoreCase(classification);
    }

    private String buildSourceDataVersion(
            SentimentSnapshotEntity currentSnapshot,
            MarketWindowType windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            int sampleCount
    ) {
        return currentSnapshot.getSourceDataVersion()
                + ";windowType=" + windowType.name()
                + ";windowStartTime=" + windowStartTime
                + ";windowEndTime=" + windowEndTime
                + ";sampleCount=" + sampleCount;
    }
}

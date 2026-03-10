package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalysisSentimentComparisonService {

    private static final int RATIO_SCALE = 8;

    private final SentimentSnapshotRepository sentimentSnapshotRepository;

    public List<AnalysisSentimentComparisonFact> buildFacts(
            SentimentSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<AnalysisSentimentComparisonFact> facts = new ArrayList<>();
        for (AnalysisComparisonReference reference : references(reportType)) {
            resolveReferenceSnapshot(currentSnapshot, reference)
                    .map(referenceSnapshot -> toFact(reference, currentSnapshot, referenceSnapshot))
                    .ifPresent(facts::add);
        }
        return facts;
    }

    private List<AnalysisComparisonReference> references(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> List.of(
                    AnalysisComparisonReference.PREV_BATCH,
                    AnalysisComparisonReference.D1,
                    AnalysisComparisonReference.D7
            );
            case MID_TERM -> List.of(
                    AnalysisComparisonReference.D7,
                    AnalysisComparisonReference.D14,
                    AnalysisComparisonReference.D30
            );
            case LONG_TERM -> List.of(
                    AnalysisComparisonReference.D30,
                    AnalysisComparisonReference.D90,
                    AnalysisComparisonReference.D180
            );
        };
    }

    private Optional<SentimentSnapshotEntity> resolveReferenceSnapshot(
            SentimentSnapshotEntity currentSnapshot,
            AnalysisComparisonReference reference
    ) {
        Instant targetTime = switch (reference) {
            case PREV_BATCH -> currentSnapshot.getSnapshotTime().minusNanos(1);
            case D1 -> currentSnapshot.getSnapshotTime().minus(1, ChronoUnit.DAYS);
            case D7 -> currentSnapshot.getSnapshotTime().minus(7, ChronoUnit.DAYS);
            case D14 -> currentSnapshot.getSnapshotTime().minus(14, ChronoUnit.DAYS);
            case D30 -> currentSnapshot.getSnapshotTime().minus(30, ChronoUnit.DAYS);
            case D90 -> currentSnapshot.getSnapshotTime().minus(90, ChronoUnit.DAYS);
            case D180 -> currentSnapshot.getSnapshotTime().minus(180, ChronoUnit.DAYS);
            default -> null;
        };

        if (targetTime == null) {
            return Optional.empty();
        }

        return sentimentSnapshotRepository
                .findTopByMetricTypeAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        SentimentMetricType.FEAR_GREED_INDEX,
                        targetTime
                );
    }

    private AnalysisSentimentComparisonFact toFact(
            AnalysisComparisonReference reference,
            SentimentSnapshotEntity currentSnapshot,
            SentimentSnapshotEntity referenceSnapshot
    ) {
        BigDecimal valueChange = currentSnapshot.getIndexValue().subtract(referenceSnapshot.getIndexValue());
        BigDecimal valueChangeRate = ratioChange(currentSnapshot.getIndexValue(), referenceSnapshot.getIndexValue());

        return new AnalysisSentimentComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                referenceSnapshot.getIndexValue(),
                referenceSnapshot.getClassification(),
                valueChange,
                valueChangeRate,
                !Objects.equals(currentSnapshot.getClassification(), referenceSnapshot.getClassification())
        );
    }

    private BigDecimal ratioChange(BigDecimal currentValue, BigDecimal referenceValue) {
        if (referenceValue == null || referenceValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return currentValue.subtract(referenceValue)
                           .divide(referenceValue, RATIO_SCALE, RoundingMode.HALF_UP);
    }
}

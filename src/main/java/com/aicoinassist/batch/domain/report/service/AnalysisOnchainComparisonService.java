package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainFactSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisOnchainComparisonService {

    private static final int RATIO_SCALE = 8;

    private final OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    public List<AnalysisOnchainComparisonFact> buildFacts(
            OnchainFactSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<AnalysisOnchainComparisonFact> facts = new ArrayList<>();
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

    private Optional<OnchainFactSnapshotEntity> resolveReferenceSnapshot(
            OnchainFactSnapshotEntity currentSnapshot,
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

        return onchainFactSnapshotRepository
                .findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        currentSnapshot.getSymbol(),
                        targetTime
                );
    }

    private AnalysisOnchainComparisonFact toFact(
            AnalysisComparisonReference reference,
            OnchainFactSnapshotEntity currentSnapshot,
            OnchainFactSnapshotEntity referenceSnapshot
    ) {
        return new AnalysisOnchainComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                referenceSnapshot.getActiveAddressCount(),
                ratioChange(currentSnapshot.getActiveAddressCount(), referenceSnapshot.getActiveAddressCount()),
                referenceSnapshot.getTransactionCount(),
                ratioChange(currentSnapshot.getTransactionCount(), referenceSnapshot.getTransactionCount()),
                referenceSnapshot.getMarketCapUsd(),
                ratioChange(currentSnapshot.getMarketCapUsd(), referenceSnapshot.getMarketCapUsd())
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

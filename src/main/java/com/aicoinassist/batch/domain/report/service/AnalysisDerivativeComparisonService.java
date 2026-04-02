package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
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

@Service
@RequiredArgsConstructor
public class AnalysisDerivativeComparisonService {

    private static final int RATIO_SCALE = 8;

    private final MarketContextSnapshotRepository marketContextSnapshotRepository;

    public List<AnalysisDerivativeComparisonFact> buildFacts(
            MarketContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<AnalysisDerivativeComparisonFact> facts = new ArrayList<>();
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
                    AnalysisComparisonReference.D3,
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

    private java.util.Optional<MarketContextSnapshotEntity> resolveReferenceSnapshot(
            MarketContextSnapshotEntity currentSnapshot,
            AnalysisComparisonReference reference
    ) {
        Instant targetTime = switch (reference) {
            case PREV_BATCH -> currentSnapshot.getSnapshotTime().minusNanos(1);
            case D1 -> currentSnapshot.getSnapshotTime().minus(1, ChronoUnit.DAYS);
            case D3 -> currentSnapshot.getSnapshotTime().minus(3, ChronoUnit.DAYS);
            case D7 -> currentSnapshot.getSnapshotTime().minus(7, ChronoUnit.DAYS);
            case D14 -> currentSnapshot.getSnapshotTime().minus(14, ChronoUnit.DAYS);
            case D30 -> currentSnapshot.getSnapshotTime().minus(30, ChronoUnit.DAYS);
            case D90 -> currentSnapshot.getSnapshotTime().minus(90, ChronoUnit.DAYS);
            case D180 -> currentSnapshot.getSnapshotTime().minus(180, ChronoUnit.DAYS);
            default -> null;
        };

        if (targetTime == null) {
            return java.util.Optional.empty();
        }

        return marketContextSnapshotRepository
                .findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        currentSnapshot.getSymbol(),
                        targetTime
                );
    }

    private AnalysisDerivativeComparisonFact toFact(
            AnalysisComparisonReference reference,
            MarketContextSnapshotEntity currentSnapshot,
            MarketContextSnapshotEntity referenceSnapshot
    ) {
        return new AnalysisDerivativeComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                referenceSnapshot.getOpenInterest(),
                ratioChange(currentSnapshot.getOpenInterest(), referenceSnapshot.getOpenInterest()),
                currentSnapshot.getLastFundingRate().subtract(referenceSnapshot.getLastFundingRate()),
                currentSnapshot.getMarkIndexBasisRate().subtract(referenceSnapshot.getMarkIndexBasisRate())
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

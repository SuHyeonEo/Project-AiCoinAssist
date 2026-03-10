package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketLevelContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
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
public class AnalysisLevelContextComparisonService {

    private static final int SCALE = 8;

    private final MarketLevelContextSnapshotRepository marketLevelContextSnapshotRepository;

    public List<AnalysisLevelContextComparisonFact> buildFacts(
            MarketLevelContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<AnalysisLevelContextComparisonFact> facts = new ArrayList<>();
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

    private Optional<MarketLevelContextSnapshotEntity> resolveReferenceSnapshot(
            MarketLevelContextSnapshotEntity currentSnapshot,
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
            return Optional.empty();
        }

        return marketLevelContextSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        currentSnapshot.getSymbol(),
                        currentSnapshot.getIntervalValue(),
                        targetTime
                );
    }

    private AnalysisLevelContextComparisonFact toFact(
            AnalysisComparisonReference reference,
            MarketLevelContextSnapshotEntity currentSnapshot,
            MarketLevelContextSnapshotEntity referenceSnapshot
    ) {
        return new AnalysisLevelContextComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                ratioChange(currentSnapshot.getSupportRepresentativePrice(), referenceSnapshot.getSupportRepresentativePrice()),
                ratioChange(currentSnapshot.getResistanceRepresentativePrice(), referenceSnapshot.getResistanceRepresentativePrice()),
                delta(currentSnapshot.getSupportZoneStrength(), referenceSnapshot.getSupportZoneStrength()),
                delta(currentSnapshot.getResistanceZoneStrength(), referenceSnapshot.getResistanceZoneStrength()),
                delta(currentSnapshot.getSupportBreakRisk(), referenceSnapshot.getSupportBreakRisk()),
                delta(currentSnapshot.getResistanceBreakRisk(), referenceSnapshot.getResistanceBreakRisk()),
                interactionType(currentSnapshot.getSupportInteractionType()),
                interactionType(referenceSnapshot.getSupportInteractionType()),
                interactionType(currentSnapshot.getResistanceInteractionType()),
                interactionType(referenceSnapshot.getResistanceInteractionType())
        );
    }

    private BigDecimal ratioChange(BigDecimal currentValue, BigDecimal referenceValue) {
        if (currentValue == null || referenceValue == null || referenceValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentValue.subtract(referenceValue)
                           .divide(referenceValue, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal delta(BigDecimal currentValue, BigDecimal referenceValue) {
        if (currentValue == null || referenceValue == null) {
            return null;
        }
        return currentValue.subtract(referenceValue).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private AnalysisPriceZoneInteractionType interactionType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return AnalysisPriceZoneInteractionType.valueOf(value);
    }
}

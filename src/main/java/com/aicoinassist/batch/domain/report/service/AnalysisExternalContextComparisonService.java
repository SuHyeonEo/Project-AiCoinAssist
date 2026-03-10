package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisExternalContextComparisonService {

    private static final BigDecimal MEDIUM_DELTA = new BigDecimal("0.50");
    private static final BigDecimal HIGH_DELTA = new BigDecimal("1.00");

    private final MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;

    public List<AnalysisExternalContextComparisonFact> buildFacts(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<AnalysisExternalContextComparisonFact> facts = new ArrayList<>();
        for (AnalysisComparisonReference reference : references(reportType)) {
            resolveReferenceSnapshot(currentSnapshot, reference)
                    .map(referenceSnapshot -> toFact(reference, currentSnapshot, referenceSnapshot))
                    .ifPresent(facts::add);
        }
        return facts;
    }

    public List<AnalysisExternalContextHighlight> buildHighlights(
            MarketExternalContextSnapshotEntity currentSnapshot,
            List<AnalysisExternalContextComparisonFact> facts
    ) {
        return facts.stream()
                    .filter(this::isHighlightCandidate)
                    .sorted(Comparator.comparing(this::highlightPriority).reversed())
                    .limit(3)
                    .map(fact -> toHighlight(currentSnapshot, fact))
                    .toList();
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

    private Optional<MarketExternalContextSnapshotEntity> resolveReferenceSnapshot(
            MarketExternalContextSnapshotEntity currentSnapshot,
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

        return marketExternalContextSnapshotRepository
                .findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        currentSnapshot.getSymbol(),
                        targetTime
                );
    }

    private AnalysisExternalContextComparisonFact toFact(
            AnalysisComparisonReference reference,
            MarketExternalContextSnapshotEntity currentSnapshot,
            MarketExternalContextSnapshotEntity referenceSnapshot
    ) {
        return new AnalysisExternalContextComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                referenceSnapshot.getCompositeRiskScore(),
                currentSnapshot.getCompositeRiskScore()
                               .subtract(referenceSnapshot.getCompositeRiskScore())
                               .setScale(8, RoundingMode.HALF_UP),
                !Objects.equals(currentSnapshot.getDominantDirection(), referenceSnapshot.getDominantDirection()),
                !Objects.equals(currentSnapshot.getHighestSeverity(), referenceSnapshot.getHighestSeverity()),
                currentSnapshot.getSupportiveSignalCount() - referenceSnapshot.getSupportiveSignalCount(),
                currentSnapshot.getCautionarySignalCount() - referenceSnapshot.getCautionarySignalCount(),
                currentSnapshot.getHeadwindSignalCount() - referenceSnapshot.getHeadwindSignalCount(),
                !Objects.equals(currentSnapshot.getPrimarySignalTitle(), referenceSnapshot.getPrimarySignalTitle()),
                referenceSnapshot.getPrimarySignalTitle()
        );
    }

    private boolean isHighlightCandidate(AnalysisExternalContextComparisonFact fact) {
        BigDecimal absDelta = fact.compositeRiskScoreDelta().abs();
        return absDelta.compareTo(MEDIUM_DELTA) >= 0
                || Boolean.TRUE.equals(fact.dominantDirectionChanged())
                || Boolean.TRUE.equals(fact.highestSeverityChanged())
                || Boolean.TRUE.equals(fact.primarySignalChanged());
    }

    private int highlightPriority(AnalysisExternalContextComparisonFact fact) {
        int priority = fact.compositeRiskScoreDelta().abs().compareTo(HIGH_DELTA) >= 0 ? 3 : 1;
        if (Boolean.TRUE.equals(fact.dominantDirectionChanged())) {
            priority += 3;
        }
        if (Boolean.TRUE.equals(fact.highestSeverityChanged())) {
            priority += 2;
        }
        if (Boolean.TRUE.equals(fact.primarySignalChanged())) {
            priority += 1;
        }
        return priority;
    }

    private AnalysisExternalContextHighlight toHighlight(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisExternalContextComparisonFact fact
    ) {
        BigDecimal delta = fact.compositeRiskScoreDelta();
        if (Boolean.TRUE.equals(fact.dominantDirectionChanged())) {
            return new AnalysisExternalContextHighlight(
                    "External regime direction changed",
                    fact.reference().name() + " 대비 external regime direction이 "
                            + label(currentSnapshot.getDominantDirection()) + "로 전환되었습니다.",
                    AnalysisContextHeadlineImportance.HIGH,
                    fact.reference()
            );
        }
        if (Boolean.TRUE.equals(fact.highestSeverityChanged())) {
            return new AnalysisExternalContextHighlight(
                    "External regime severity shifted",
                    fact.reference().name() + " 대비 최고 severity가 "
                            + label(currentSnapshot.getHighestSeverity()) + "로 바뀌었습니다.",
                    AnalysisContextHeadlineImportance.HIGH,
                    fact.reference()
            );
        }
        if (Boolean.TRUE.equals(fact.primarySignalChanged())) {
            return new AnalysisExternalContextHighlight(
                    "Primary external signal changed",
                    fact.reference().name() + "의 " + nullSafe(fact.referencePrimarySignalTitle())
                            + "에서 " + nullSafe(currentSnapshot.getPrimarySignalTitle()) + "로 주요 external signal이 바뀌었습니다.",
                    AnalysisContextHeadlineImportance.MEDIUM,
                    fact.reference()
            );
        }
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            return new AnalysisExternalContextHighlight(
                    "External risk intensified",
                    fact.reference().name() + " 대비 composite risk score가 "
                            + signed(delta) + "p 상승했습니다.",
                    delta.abs().compareTo(HIGH_DELTA) >= 0
                            ? AnalysisContextHeadlineImportance.HIGH
                            : AnalysisContextHeadlineImportance.MEDIUM,
                    fact.reference()
            );
        }
        return new AnalysisExternalContextHighlight(
                "External risk eased",
                fact.reference().name() + " 대비 composite risk score가 "
                        + signed(delta) + "p 완화되었습니다.",
                AnalysisContextHeadlineImportance.MEDIUM,
                fact.reference()
        );
    }

    private String signed(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String label(String value) {
        return value == null ? "unknown" : value.toLowerCase().replace('_', ' ');
    }

    private String nullSafe(String value) {
        return value == null ? "unknown signal" : value;
    }
}

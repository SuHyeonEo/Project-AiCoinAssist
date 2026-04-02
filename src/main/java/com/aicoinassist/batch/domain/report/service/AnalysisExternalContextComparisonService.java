package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextWindowSummary;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimePersistence;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeTransition;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeTransitionType;
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

    public List<AnalysisExternalRegimeTransition> buildTransitions(
            MarketExternalContextSnapshotEntity currentSnapshot,
            List<AnalysisExternalContextComparisonFact> facts
    ) {
        return facts.stream()
                .map(fact -> toTransition(currentSnapshot, fact))
                .toList();
    }

    public AnalysisExternalRegimePersistence buildPersistence(
            MarketExternalContextSnapshotEntity currentSnapshot,
            List<AnalysisExternalContextWindowSummary> windowSummaries
    ) {
        if (windowSummaries == null || windowSummaries.isEmpty()) {
            return null;
        }

        AnalysisExternalContextWindowSummary primaryWindowSummary = windowSummaries.get(windowSummaries.size() - 1);
        BigDecimal dominantDirectionShare = dominantDirectionShare(
                currentSnapshot.getDominantDirection(),
                primaryWindowSummary
        );
        BigDecimal highSeverityShare = ratio(
                primaryWindowSummary.highSeveritySampleCount(),
                primaryWindowSummary.sampleCount()
        );
        BigDecimal persistenceScore = dominantDirectionShare.multiply(new BigDecimal("0.70"))
                .add(highSeverityShare.multiply(new BigDecimal("0.30")))
                .setScale(8, RoundingMode.HALF_UP);

        if (currentSnapshot.getDominantDirection() == null) {
            return new AnalysisExternalRegimePersistence(
                    primaryWindowSummary.windowType(),
                    null,
                    dominantDirectionShare,
                    highSeverityShare,
                    persistenceScore,
                    primaryWindowSummary.windowType().name()
                            + " 기준으로 외부 우세 방향은 아직 뚜렷하지 않으며, 높은 심각도 비중은 "
                            + ratioLabel(highSeverityShare)
                            + "입니다."
            );
        }

        return new AnalysisExternalRegimePersistence(
                primaryWindowSummary.windowType(),
                enumValue(currentSnapshot.getDominantDirection(), AnalysisExternalRegimeDirection.class),
                dominantDirectionShare,
                highSeverityShare,
                persistenceScore,
                primaryWindowSummary.windowType().name()
                        + " 기준 외부 우세 방향은 "
                        + koreanDirection(currentSnapshot.getDominantDirection())
                        + "이며, 해당 방향 비중은 "
                        + ratioLabel(dominantDirectionShare)
                        + ", 높은 심각도 비중은 "
                        + ratioLabel(highSeverityShare)
                        + "입니다."
        );
    }

    public AnalysisExternalRegimeStatePayload buildState(
            MarketExternalContextSnapshotEntity currentSnapshot,
            List<AnalysisExternalRegimeTransition> transitions,
            AnalysisExternalRegimePersistence persistence,
            List<AnalysisExternalContextWindowSummary> windowSummaries
    ) {
        BigDecimal reversalRiskScore = reversalRiskScore(transitions, persistence, windowSummaries);
        String summary;
        if (currentSnapshot.getDominantDirection() == null && currentSnapshot.getPrimarySignalTitle() == null) {
            summary = "외부 요인 전반에서 뚜렷한 우세 방향 신호는 아직 확인되지 않으며, 반전 위험은 "
                    + reversalRiskScore.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                    + "입니다.";
        } else {
            summary = "외부 체계는 "
                    + koreanDirection(currentSnapshot.getDominantDirection())
                    + "이며, 심각도는 "
                    + koreanSeverity(currentSnapshot.getHighestSeverity())
                    + "입니다. 핵심 신호는 "
                    + nullSafe(currentSnapshot.getPrimarySignalTitle())
                    + "이며, 반전 위험은 "
                    + reversalRiskScore.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                    + "입니다.";
        }
        return new AnalysisExternalRegimeStatePayload(
                enumValue(currentSnapshot.getDominantDirection(), AnalysisExternalRegimeDirection.class),
                enumValue(currentSnapshot.getHighestSeverity(), AnalysisExternalRegimeSeverity.class),
                enumValue(currentSnapshot.getPrimarySignalCategory(), com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory.class),
                currentSnapshot.getPrimarySignalTitle(),
                currentSnapshot.getCompositeRiskScore(),
                reversalRiskScore,
                summary
        );
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
                    fact.reference().name() + " external regime direction shifted to "
                            + label(currentSnapshot.getDominantDirection()) + ".",
                    AnalysisContextHeadlineImportance.HIGH,
                    fact.reference()
            );
        }
        if (Boolean.TRUE.equals(fact.highestSeverityChanged())) {
            return new AnalysisExternalContextHighlight(
                    "External regime severity shifted",
                    fact.reference().name() + " highest severity shifted to "
                            + label(currentSnapshot.getHighestSeverity()) + ".",
                    AnalysisContextHeadlineImportance.HIGH,
                    fact.reference()
            );
        }
        if (Boolean.TRUE.equals(fact.primarySignalChanged())) {
            return new AnalysisExternalContextHighlight(
                    "Primary external signal changed",
                    "Primary external signal changed from " + nullSafe(fact.referencePrimarySignalTitle())
                            + " at " + fact.reference().name() + " to " + nullSafe(currentSnapshot.getPrimarySignalTitle()) + ".",
                    AnalysisContextHeadlineImportance.MEDIUM,
                    fact.reference()
            );
        }
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            return new AnalysisExternalContextHighlight(
                    "External risk intensified",
                    fact.reference().name() + " composite risk score increased by "
                            + signed(delta) + "p.",
                    delta.abs().compareTo(HIGH_DELTA) >= 0
                            ? AnalysisContextHeadlineImportance.HIGH
                            : AnalysisContextHeadlineImportance.MEDIUM,
                    fact.reference()
            );
        }
        return new AnalysisExternalContextHighlight(
                "External risk eased",
                fact.reference().name() + " composite risk score eased by "
                        + signed(delta) + "p.",
                AnalysisContextHeadlineImportance.MEDIUM,
                fact.reference()
        );
    }

    private AnalysisExternalRegimeTransition toTransition(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisExternalContextComparisonFact fact
    ) {
        AnalysisExternalRegimeTransitionType transitionType = transitionType(currentSnapshot, fact);
        return new AnalysisExternalRegimeTransition(
                fact.reference(),
                fact.referenceTime(),
                transitionType,
                enumValue(currentSnapshot.getDominantDirection(), AnalysisExternalRegimeDirection.class),
                enumValue(currentSnapshot.getHighestSeverity(), AnalysisExternalRegimeSeverity.class),
                fact.compositeRiskScoreDelta(),
                transitionSummary(currentSnapshot, fact, transitionType)
        );
    }

    private AnalysisExternalRegimeTransitionType transitionType(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisExternalContextComparisonFact fact
    ) {
        if (Boolean.TRUE.equals(fact.dominantDirectionChanged())) {
            String dominantDirection = currentSnapshot.getDominantDirection();
            if (dominantDirection == null) {
                return AnalysisExternalRegimeTransitionType.STABLE;
            }
            return switch (dominantDirection) {
                case "SUPPORTIVE" -> AnalysisExternalRegimeTransitionType.TRANSITION_TO_SUPPORTIVE;
                case "CAUTIONARY" -> AnalysisExternalRegimeTransitionType.TRANSITION_TO_CAUTIONARY;
                case "HEADWIND" -> AnalysisExternalRegimeTransitionType.TRANSITION_TO_HEADWIND;
                default -> AnalysisExternalRegimeTransitionType.STABLE;
            };
        }
        if (fact.compositeRiskScoreDelta().compareTo(BigDecimal.ZERO) > 0) {
            return AnalysisExternalRegimeTransitionType.INTENSIFYING;
        }
        if (fact.compositeRiskScoreDelta().compareTo(BigDecimal.ZERO) < 0) {
            return AnalysisExternalRegimeTransitionType.EASING;
        }
        return AnalysisExternalRegimeTransitionType.STABLE;
    }

    private String transitionSummary(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisExternalContextComparisonFact fact,
            AnalysisExternalRegimeTransitionType transitionType
    ) {
        return switch (transitionType) {
            case TRANSITION_TO_SUPPORTIVE, TRANSITION_TO_CAUTIONARY, TRANSITION_TO_HEADWIND ->
                    fact.reference().name() + " external regime transitioned to "
                            + label(currentSnapshot.getDominantDirection())
                            + ".";
            case INTENSIFYING ->
                    fact.reference().name() + " composite external risk expanded by "
                            + signed(fact.compositeRiskScoreDelta()) + "p.";
            case EASING ->
                    fact.reference().name() + " composite external risk eased by "
                            + signed(fact.compositeRiskScoreDelta()) + "p.";
            case STABLE ->
                    fact.reference().name() + " external regime remains broadly stable.";
        };
    }

    private BigDecimal reversalRiskScore(
            List<AnalysisExternalRegimeTransition> transitions,
            AnalysisExternalRegimePersistence persistence,
            List<AnalysisExternalContextWindowSummary> windowSummaries
    ) {
        BigDecimal transitionComponent = transitions == null || transitions.isEmpty()
                ? BigDecimal.ZERO
                : transitions.stream()
                .filter(transition -> transition.transitionType() == AnalysisExternalRegimeTransitionType.TRANSITION_TO_SUPPORTIVE
                        || transition.transitionType() == AnalysisExternalRegimeTransitionType.TRANSITION_TO_CAUTIONARY
                        || transition.transitionType() == AnalysisExternalRegimeTransitionType.TRANSITION_TO_HEADWIND)
                .findFirst()
                .map(transition -> new BigDecimal("0.30"))
                .orElse(BigDecimal.ZERO);
        BigDecimal persistenceComponent = persistence == null
                ? new BigDecimal("0.50")
                : BigDecimal.ONE.subtract(persistence.persistenceScore()).max(BigDecimal.ZERO);
        BigDecimal windowDeviationComponent = windowSummaries == null || windowSummaries.isEmpty()
                || windowSummaries.get(windowSummaries.size() - 1).currentCompositeRiskVsAverage() == null
                ? BigDecimal.ZERO
                : windowSummaries.get(windowSummaries.size() - 1).currentCompositeRiskVsAverage().abs()
                        .min(BigDecimal.ONE)
                        .multiply(new BigDecimal("0.40"));

        return transitionComponent
                .add(persistenceComponent.multiply(new BigDecimal("0.40")))
                .add(windowDeviationComponent)
                .min(BigDecimal.ONE)
                .setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal dominantDirectionShare(
            String dominantDirection,
            AnalysisExternalContextWindowSummary primaryWindowSummary
    ) {
        if (primaryWindowSummary.sampleCount() == null || primaryWindowSummary.sampleCount() == 0) {
            return BigDecimal.ZERO;
        }
        if (dominantDirection == null) {
            return BigDecimal.ZERO;
        }
        int dominantSampleCount = switch (dominantDirection) {
            case "SUPPORTIVE" -> primaryWindowSummary.supportiveDominanceSampleCount();
            case "CAUTIONARY" -> primaryWindowSummary.cautionaryDominanceSampleCount();
            case "HEADWIND" -> primaryWindowSummary.headwindDominanceSampleCount();
            default -> 0;
        };
        return ratio(dominantSampleCount, primaryWindowSummary.sampleCount());
    }

    private BigDecimal ratio(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator.longValue())
                .divide(BigDecimal.valueOf(denominator.longValue()), 8, RoundingMode.HALF_UP);
    }

    private String signed(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String ratioLabel(BigDecimal value) {
        return value.multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String label(String value) {
        return value == null ? "unknown" : value.toLowerCase().replace('_', ' ');
    }

    private String nullSafe(String value) {
        return value == null ? "뚜렷한 핵심 신호가 확인되지 않았습니다" : value;
    }

    private String koreanDirection(String value) {
        if (value == null) {
            return "뚜렷하지 않은 상태";
        }
        return switch (value) {
            case "SUPPORTIVE" -> "우호 우세";
            case "CAUTIONARY" -> "경계 우세";
            case "HEADWIND" -> "하방 부담 우세";
            default -> value.toLowerCase().replace('_', ' ');
        };
    }

    private String koreanSeverity(String value) {
        if (value == null) {
            return "뚜렷하지 않은 상태";
        }
        return switch (value) {
            case "LOW" -> "낮은 편";
            case "MEDIUM" -> "보통";
            case "HIGH" -> "높은 편";
            default -> value.toLowerCase().replace('_', ' ');
        };
    }

    private <T extends Enum<T>> T enumValue(String value, Class<T> enumClass) {
        return value == null ? null : Enum.valueOf(enumClass, value);
    }
}

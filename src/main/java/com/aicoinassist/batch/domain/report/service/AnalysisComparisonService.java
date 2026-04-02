package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
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
public class AnalysisComparisonService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    public List<AnalysisComparisonFact> buildFacts(
            MarketIndicatorSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        return switch (reportType) {
            case SHORT_TERM -> shortTermFacts(currentSnapshot);
            case MID_TERM -> midTermFacts(currentSnapshot);
            case LONG_TERM -> longTermFacts(currentSnapshot);
        };
    }

    private List<AnalysisComparisonFact> shortTermFacts(MarketIndicatorSnapshotEntity currentSnapshot) {
        List<AnalysisComparisonFact> facts = new ArrayList<>(buildTimeWindowFacts(
                currentSnapshot,
                List.of(
                        new TimeWindowReference(AnalysisComparisonReference.D1, 1),
                        new TimeWindowReference(AnalysisComparisonReference.D3, 3),
                        new TimeWindowReference(AnalysisComparisonReference.D7, 7)
                )
        ));

        addIfPresent(
                facts,
                0,
                AnalysisComparisonReference.PREV_BATCH,
                marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
                        currentSnapshot.getSymbol(),
                        currentSnapshot.getIntervalValue(),
                        currentSnapshot.getSnapshotTime()
                ),
                currentSnapshot
        );

        return facts;
    }

    private List<AnalysisComparisonFact> midTermFacts(MarketIndicatorSnapshotEntity currentSnapshot) {
        return new ArrayList<>(buildTimeWindowFacts(
                currentSnapshot,
                List.of(
                        new TimeWindowReference(AnalysisComparisonReference.D7, 7),
                        new TimeWindowReference(AnalysisComparisonReference.D14, 14),
                        new TimeWindowReference(AnalysisComparisonReference.D30, 30)
                )
        ));
    }

    private List<AnalysisComparisonFact> longTermFacts(MarketIndicatorSnapshotEntity currentSnapshot) {
        List<AnalysisComparisonFact> facts = new ArrayList<>(buildTimeWindowFacts(
                currentSnapshot,
                List.of(
                        new TimeWindowReference(AnalysisComparisonReference.D30, 30),
                        new TimeWindowReference(AnalysisComparisonReference.D90, 90),
                        new TimeWindowReference(AnalysisComparisonReference.D180, 180)
                )
        ));

        addYear52ExtremumFacts(facts, currentSnapshot);

        return facts;
    }

    private void addYear52ExtremumFacts(
            List<AnalysisComparisonFact> facts,
            MarketIndicatorSnapshotEntity currentSnapshot
    ) {
        Instant snapshotTimeFrom = currentSnapshot.getSnapshotTime().minus(364, ChronoUnit.DAYS);
        Instant snapshotTimeTo = currentSnapshot.getSnapshotTime();

        addIfPresent(
                facts,
                facts.size(),
                AnalysisComparisonReference.Y52_HIGH,
                marketIndicatorSnapshotRepository
                        .findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceDescSnapshotTimeDescIdDesc(
                                currentSnapshot.getSymbol(),
                                currentSnapshot.getIntervalValue(),
                                snapshotTimeFrom,
                                snapshotTimeTo
                        ),
                currentSnapshot
        );
        addIfPresent(
                facts,
                facts.size(),
                AnalysisComparisonReference.Y52_LOW,
                marketIndicatorSnapshotRepository
                        .findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceAscSnapshotTimeDescIdDesc(
                                currentSnapshot.getSymbol(),
                                currentSnapshot.getIntervalValue(),
                                snapshotTimeFrom,
                                snapshotTimeTo
                        ),
                currentSnapshot
        );
    }

    private List<AnalysisComparisonFact> buildTimeWindowFacts(
            MarketIndicatorSnapshotEntity currentSnapshot,
            List<TimeWindowReference> references
    ) {
        List<AnalysisComparisonFact> facts = new ArrayList<>();
        String symbol = currentSnapshot.getSymbol();
        String intervalValue = currentSnapshot.getIntervalValue();
        Instant snapshotTime = currentSnapshot.getSnapshotTime();

        for (TimeWindowReference reference : references) {
            addIfPresent(
                    facts,
                    facts.size(),
                    reference.reference(),
                    marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                            symbol,
                            intervalValue,
                            snapshotTime.minus(reference.days(), ChronoUnit.DAYS)
                    ),
                    currentSnapshot
            );
        }

        return facts;
    }

    private void addIfPresent(
            List<AnalysisComparisonFact> facts,
            int index,
            AnalysisComparisonReference reference,
            Optional<MarketIndicatorSnapshotEntity> referenceSnapshot,
            MarketIndicatorSnapshotEntity currentSnapshot
    ) {
        referenceSnapshot.map(snapshot -> createFact(reference, snapshot, currentSnapshot))
                         .ifPresent(fact -> facts.add(index, fact));
    }

    private record TimeWindowReference(
            AnalysisComparisonReference reference,
            long days
    ) {
    }

    private AnalysisComparisonFact createFact(
            AnalysisComparisonReference reference,
            MarketIndicatorSnapshotEntity referenceSnapshot,
            MarketIndicatorSnapshotEntity currentSnapshot
    ) {
        return new AnalysisComparisonFact(
                reference,
                referenceSnapshot.getSnapshotTime(),
                referenceSnapshot.getCurrentPrice(),
                changeRate(currentSnapshot.getCurrentPrice(), referenceSnapshot.getCurrentPrice()),
                currentSnapshot.getRsi14().subtract(referenceSnapshot.getRsi14()),
                currentSnapshot.getMacdHistogram().subtract(referenceSnapshot.getMacdHistogram()),
                changeRate(currentSnapshot.getAtr14(), referenceSnapshot.getAtr14())
        );
    }

    private BigDecimal changeRate(BigDecimal currentValue, BigDecimal referenceValue) {
        if (referenceValue == null || referenceValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentValue.subtract(referenceValue)
                           .multiply(ONE_HUNDRED)
                           .divide(referenceValue, 4, RoundingMode.HALF_UP);
    }
}

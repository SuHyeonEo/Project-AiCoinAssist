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
            case MID_TERM, LONG_TERM -> List.of();
        };
    }

    private List<AnalysisComparisonFact> shortTermFacts(MarketIndicatorSnapshotEntity currentSnapshot) {
        List<AnalysisComparisonFact> facts = new ArrayList<>();
        String symbol = currentSnapshot.getSymbol();
        String intervalValue = currentSnapshot.getIntervalValue();
        Instant snapshotTime = currentSnapshot.getSnapshotTime();

        addIfPresent(
                facts,
                AnalysisComparisonReference.PREV_BATCH,
                marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
                        symbol,
                        intervalValue,
                        snapshotTime
                ),
                currentSnapshot
        );
        addIfPresent(
                facts,
                AnalysisComparisonReference.D1,
                marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        symbol,
                        intervalValue,
                        snapshotTime.minus(1, ChronoUnit.DAYS)
                ),
                currentSnapshot
        );
        addIfPresent(
                facts,
                AnalysisComparisonReference.D3,
                marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        symbol,
                        intervalValue,
                        snapshotTime.minus(3, ChronoUnit.DAYS)
                ),
                currentSnapshot
        );
        addIfPresent(
                facts,
                AnalysisComparisonReference.D7,
                marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                        symbol,
                        intervalValue,
                        snapshotTime.minus(7, ChronoUnit.DAYS)
                ),
                currentSnapshot
        );

        return facts;
    }

    private void addIfPresent(
            List<AnalysisComparisonFact> facts,
            AnalysisComparisonReference reference,
            Optional<MarketIndicatorSnapshotEntity> referenceSnapshot,
            MarketIndicatorSnapshotEntity currentSnapshot
    ) {
        referenceSnapshot.map(snapshot -> createFact(reference, snapshot, currentSnapshot))
                         .ifPresent(facts::add);
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

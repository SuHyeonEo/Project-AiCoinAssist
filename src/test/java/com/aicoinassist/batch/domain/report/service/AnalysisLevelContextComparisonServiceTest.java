package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketLevelContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisLevelContextComparisonServiceTest {

    @Mock
    private MarketLevelContextSnapshotRepository marketLevelContextSnapshotRepository;

    @Test
    void buildFactsReturnsShortTermLevelContextComparisons() {
        AnalysisLevelContextComparisonService service = new AnalysisLevelContextComparisonService(
                marketLevelContextSnapshotRepository
        );

        MarketLevelContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                new BigDecimal("86850.00000000"),
                new BigDecimal("88560.00000000"),
                new BigDecimal("0.89285714"),
                new BigDecimal("0.86285714"),
                new BigDecimal("0.18000000"),
                new BigDecimal("0.05000000"),
                "ABOVE_ZONE",
                "BELOW_ZONE"
        );

        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-09T00:59:58.999999999Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T23:59:59Z"),
                new BigDecimal("86600.00000000"),
                new BigDecimal("88420.00000000"),
                new BigDecimal("0.85000000"),
                new BigDecimal("0.83000000"),
                new BigDecimal("0.14000000"),
                new BigDecimal("0.07000000"),
                "INSIDE_ZONE",
                "BELOW_ZONE"
        )));
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-08T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:59:59Z"),
                new BigDecimal("86000.00000000"),
                new BigDecimal("89000.00000000"),
                new BigDecimal("0.81000000"),
                new BigDecimal("0.90000000"),
                new BigDecimal("0.10000000"),
                new BigDecimal("0.09000000"),
                "ABOVE_ZONE",
                "INSIDE_ZONE"
        )));
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-06T00:59:59Z")
        )).thenReturn(Optional.empty());
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-02T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-02T00:59:59Z"),
                new BigDecimal("84500.00000000"),
                new BigDecimal("90200.00000000"),
                new BigDecimal("0.76000000"),
                new BigDecimal("0.92000000"),
                new BigDecimal("0.22000000"),
                new BigDecimal("0.11000000"),
                "ABOVE_ZONE",
                "BELOW_ZONE"
        )));

        List<AnalysisLevelContextComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).extracting(AnalysisLevelContextComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.PREV_BATCH,
                                 AnalysisComparisonReference.D1,
                                 AnalysisComparisonReference.D7
                         );
        assertThat(facts.get(0).supportRepresentativePriceChangeRate()).isEqualByComparingTo("0.00288684");
        assertThat(facts.get(1).resistanceStrengthDelta()).isEqualByComparingTo("-0.03714286");
        assertThat(facts.get(1).referenceResistanceInteractionType()).isNotEqualTo(facts.get(1).currentResistanceInteractionType());
        assertThat(facts.get(2).supportBreakRiskDelta()).isEqualByComparingTo("-0.04000000");
    }

    @Test
    void buildFactsReturnsEmptyWhenNoMidTermAnchorExists() {
        AnalysisLevelContextComparisonService service = new AnalysisLevelContextComparisonService(
                marketLevelContextSnapshotRepository
        );

        MarketLevelContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                new BigDecimal("86850.00000000"),
                new BigDecimal("88560.00000000"),
                new BigDecimal("0.89285714"),
                new BigDecimal("0.86285714"),
                new BigDecimal("0.18000000"),
                new BigDecimal("0.05000000"),
                "ABOVE_ZONE",
                "BELOW_ZONE"
        );

        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-02T00:59:59Z")
        )).thenReturn(Optional.empty());
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-02-23T00:59:59Z")
        )).thenReturn(Optional.empty());
        when(marketLevelContextSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-02-07T00:59:59Z")
        )).thenReturn(Optional.empty());

        assertThat(service.buildFacts(currentSnapshot, AnalysisReportType.MID_TERM)).isEmpty();
    }

    private MarketLevelContextSnapshotEntity snapshot(
            Instant snapshotTime,
            BigDecimal supportRepresentativePrice,
            BigDecimal resistanceRepresentativePrice,
            BigDecimal supportStrength,
            BigDecimal resistanceStrength,
            BigDecimal supportBreakRisk,
            BigDecimal resistanceBreakRisk,
            String supportInteractionType,
            String resistanceInteractionType
    ) {
        return MarketLevelContextSnapshotEntity.builder()
                                               .symbol("BTCUSDT")
                                               .intervalValue("1h")
                                               .snapshotTime(snapshotTime)
                                               .currentPrice(new BigDecimal("87500.00000000"))
                                               .supportZoneRank(1)
                                               .supportRepresentativePrice(supportRepresentativePrice)
                                               .supportZoneLow(new BigDecimal("86200.00000000"))
                                               .supportZoneHigh(new BigDecimal("86800.00000000"))
                                               .supportDistanceToZone(new BigDecimal("0.00400000"))
                                               .supportZoneStrength(supportStrength)
                                               .supportInteractionType(supportInteractionType)
                                               .supportRecentTestCount(5)
                                               .supportRecentRejectionCount(4)
                                               .supportRecentBreakCount(1)
                                               .supportBreakRisk(supportBreakRisk)
                                               .resistanceZoneRank(1)
                                               .resistanceRepresentativePrice(resistanceRepresentativePrice)
                                               .resistanceZoneLow(new BigDecimal("88400.00000000"))
                                               .resistanceZoneHigh(new BigDecimal("88800.00000000"))
                                               .resistanceDistanceToZone(new BigDecimal("0.01000000"))
                                               .resistanceZoneStrength(resistanceStrength)
                                               .resistanceInteractionType(resistanceInteractionType)
                                               .resistanceRecentTestCount(3)
                                               .resistanceRecentRejectionCount(2)
                                               .resistanceRecentBreakCount(0)
                                               .resistanceBreakRisk(resistanceBreakRisk)
                                               .sourceDataVersion("indicator=basis-key;supportZone=support-v1;resistanceZone=resistance-v1")
                                               .build();
    }
}

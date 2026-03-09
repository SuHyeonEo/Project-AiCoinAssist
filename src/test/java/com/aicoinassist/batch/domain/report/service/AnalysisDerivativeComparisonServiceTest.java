package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
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
class AnalysisDerivativeComparisonServiceTest {

    @Mock
    private MarketContextSnapshotRepository marketContextSnapshotRepository;

    @Test
    void buildFactsReturnsShortTermDerivativeComparisons() {
        AnalysisDerivativeComparisonService service = new AnalysisDerivativeComparisonService(marketContextSnapshotRepository);

        MarketContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:30Z"),
                new BigDecimal("12345.67890000"),
                new BigDecimal("0.00045000"),
                new BigDecimal("0.12000000")
        );

        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-09T00:59:29.999999999Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T23:59:30Z"),
                new BigDecimal("12000.00000000"),
                new BigDecimal("0.00035000"),
                new BigDecimal("0.10000000")
        )));
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-08T00:59:30Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:59:30Z"),
                new BigDecimal("11800.00000000"),
                new BigDecimal("0.00031000"),
                new BigDecimal("0.08500000")
        )));
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-06T00:59:30Z")
        )).thenReturn(Optional.empty());
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-02T00:59:30Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-02T00:59:30Z"),
                new BigDecimal("11000.00000000"),
                new BigDecimal("0.00020000"),
                new BigDecimal("0.06000000")
        )));

        List<AnalysisDerivativeComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).extracting(AnalysisDerivativeComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.PREV_BATCH,
                                 AnalysisComparisonReference.D1,
                                 AnalysisComparisonReference.D7
                         );
        assertThat(facts.get(0).openInterestChangeRate()).isEqualByComparingTo("0.02880658");
        assertThat(facts.get(1).fundingRateDelta()).isEqualByComparingTo("0.00014000");
        assertThat(facts.get(2).basisRateDelta()).isEqualByComparingTo("0.06000000");
    }

    @Test
    void buildFactsReturnsEmptyWhenNoMidTermAnchorExists() {
        AnalysisDerivativeComparisonService service = new AnalysisDerivativeComparisonService(marketContextSnapshotRepository);

        MarketContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:30Z"),
                new BigDecimal("12345.67890000"),
                new BigDecimal("0.00045000"),
                new BigDecimal("0.12000000")
        );

        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-02T00:59:30Z")
        )).thenReturn(Optional.empty());
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-02-23T00:59:30Z")
        )).thenReturn(Optional.empty());
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-02-07T00:59:30Z")
        )).thenReturn(Optional.empty());

        assertThat(service.buildFacts(currentSnapshot, AnalysisReportType.MID_TERM)).isEmpty();
    }

    private MarketContextSnapshotEntity snapshot(
            Instant snapshotTime,
            BigDecimal openInterest,
            BigDecimal fundingRate,
            BigDecimal basisRate
    ) {
        return MarketContextSnapshotEntity.builder()
                                          .symbol("BTCUSDT")
                                          .snapshotTime(snapshotTime)
                                          .openInterestSourceEventTime(snapshotTime.minusSeconds(30))
                                          .premiumIndexSourceEventTime(snapshotTime)
                                          .sourceDataVersion("context-basis-key")
                                          .openInterest(openInterest)
                                          .markPrice(new BigDecimal("87500.12000000"))
                                          .indexPrice(new BigDecimal("87480.02000000"))
                                          .lastFundingRate(fundingRate)
                                          .nextFundingTime(Instant.parse("2026-03-09T08:00:00Z"))
                                          .markIndexBasisRate(basisRate)
                                          .build();
    }
}

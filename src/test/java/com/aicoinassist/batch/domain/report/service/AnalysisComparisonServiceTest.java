package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
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
class AnalysisComparisonServiceTest {

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Test
    void buildFactsCreatesShortTermComparisonFactsFromReferenceSnapshots() {
        AnalysisComparisonService service = new AnalysisComparisonService(marketIndicatorSnapshotRepository);

        MarketIndicatorSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                "1h",
                "87500",
                "62",
                "20",
                "1500"
        );

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-08T23:59:59Z"), "1h", "87000", "60", "15", "1400")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-08T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-08T00:59:59Z"), "1h", "86000", "55", "10", "1300")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-06T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-06T00:59:59Z"), "1h", "84000", "50", "5", "1200")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-02T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-02T00:59:59Z"), "1h", "80000", "48", "-5", "1100")));

        List<AnalysisComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).hasSize(4);
        assertThat(facts).extracting(AnalysisComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.PREV_BATCH,
                                 AnalysisComparisonReference.D1,
                                 AnalysisComparisonReference.D3,
                                 AnalysisComparisonReference.D7
                         );
        assertThat(facts.get(0).priceChangeRate()).isEqualByComparingTo("0.5747");
        assertThat(facts.get(1).priceChangeRate()).isEqualByComparingTo("1.7442");
        assertThat(facts.get(1).rsiDelta()).isEqualByComparingTo("7");
        assertThat(facts.get(3).macdHistogramDelta()).isEqualByComparingTo("25");
        assertThat(facts.get(3).atrChangeRate()).isEqualByComparingTo("36.3636");
    }

    @Test
    void buildFactsReturnsEmptyForUnsupportedComparisonWindow() {
        AnalysisComparisonService service = new AnalysisComparisonService(marketIndicatorSnapshotRepository);

        List<AnalysisComparisonFact> facts = service.buildFacts(
                snapshot(Instant.parse("2026-03-09T00:59:59Z"), "4h", "87500", "62", "20", "1500"),
                AnalysisReportType.MID_TERM
        );

        assertThat(facts).isEmpty();
    }

    private MarketIndicatorSnapshotEntity snapshot(
            Instant snapshotTime,
            String intervalValue,
            String currentPrice,
            String rsi14,
            String macdHistogram,
            String atr14
    ) {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue(intervalValue)
                                            .snapshotTime(snapshotTime)
                                            .latestCandleOpenTime(snapshotTime.minusSeconds(3600))
                                            .priceSourceEventTime(snapshotTime.minusSeconds(30))
                                            .sourceDataVersion("basis-key-" + snapshotTime)
                                            .currentPrice(new BigDecimal(currentPrice))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86000"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal(rsi14))
                                            .macdLine(new BigDecimal("120"))
                                            .macdSignalLine(new BigDecimal("100"))
                                            .macdHistogram(new BigDecimal(macdHistogram))
                                            .atr14(new BigDecimal(atr14))
                                            .bollingerUpperBand(new BigDecimal("88500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("85500"))
                                            .build();
    }
}

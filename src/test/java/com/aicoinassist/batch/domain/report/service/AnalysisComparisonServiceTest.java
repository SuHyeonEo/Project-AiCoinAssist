package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
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

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    @Test
    void buildFactsCreatesShortTermComparisonFactsFromReferenceSnapshots() {
        AnalysisComparisonService service = new AnalysisComparisonService(
                marketIndicatorSnapshotRepository,
                analysisReportRepository
        );

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
    void buildFactsReturnsEmptyWhenReferenceSnapshotsAreMissing() {
        AnalysisComparisonService service = new AnalysisComparisonService(
                marketIndicatorSnapshotRepository,
                analysisReportRepository
        );

        List<AnalysisComparisonFact> facts = service.buildFacts(
                snapshot(Instant.parse("2026-03-09T00:59:59Z"), "4h", "87500", "62", "20", "1500"),
                AnalysisReportType.MID_TERM
        );

        assertThat(facts).isEmpty();
    }

    @Test
    void buildFactsCreatesMidTermComparisonFactsFromTimeWindows() {
        AnalysisComparisonService service = new AnalysisComparisonService(
                marketIndicatorSnapshotRepository,
                analysisReportRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                "4h",
                "87500",
                "62",
                "20",
                "1500"
        );

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "4h",
                Instant.parse("2026-03-02T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-02T00:59:59Z"), "4h", "80000", "48", "-5", "1100")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "4h",
                Instant.parse("2026-02-23T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-02-22T20:59:59Z"), "4h", "78000", "45", "-10", "1000")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "4h",
                Instant.parse("2026-02-07T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-02-06T20:59:59Z"), "4h", "74000", "43", "-20", "900")));
        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(previousReport(
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-01T20:59:59Z")
        )));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "4h",
                Instant.parse("2026-03-01T20:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-03-01T20:59:59Z"), "4h", "79000", "46", "-8", "1050")));

        List<AnalysisComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.MID_TERM);

        assertThat(facts).hasSize(4);
        assertThat(facts).extracting(AnalysisComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.D7,
                                 AnalysisComparisonReference.D14,
                                 AnalysisComparisonReference.D30,
                                 AnalysisComparisonReference.PREV_MID_REPORT
                         );
        assertThat(facts.get(0).priceChangeRate()).isEqualByComparingTo("9.3750");
        assertThat(facts.get(1).rsiDelta()).isEqualByComparingTo("17");
        assertThat(facts.get(2).macdHistogramDelta()).isEqualByComparingTo("40");
        assertThat(facts.get(3).referenceTime()).isEqualTo(Instant.parse("2026-03-01T20:59:59Z"));
        assertThat(facts.get(3).priceChangeRate()).isEqualByComparingTo("10.7595");
    }

    @Test
    void buildFactsCreatesLongTermComparisonFactsFromTimeWindows() {
        AnalysisComparisonService service = new AnalysisComparisonService(
                marketIndicatorSnapshotRepository,
                analysisReportRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                "1d",
                "87500",
                "62",
                "20",
                "1500"
        );

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2026-02-07T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-02-07T00:00:00Z"), "1d", "74000", "43", "-20", "900")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2025-12-09T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2025-12-09T00:00:00Z"), "1d", "68000", "40", "-30", "800")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2025-09-10T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2025-09-10T00:00:00Z"), "1d", "60000", "35", "-40", "700")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceDescSnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2025-03-10T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2025-11-20T00:00:00Z"), "1d", "92000", "68", "30", "980")));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderByCurrentPriceAscSnapshotTimeDescIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2025-03-10T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2025-05-15T00:00:00Z"), "1d", "52000", "28", "-55", "760")));
        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(previousReport(
                AnalysisReportType.LONG_TERM,
                Instant.parse("2026-01-15T00:00:00Z")
        )));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "1d",
                Instant.parse("2026-01-15T00:00:00Z")
        )).thenReturn(Optional.of(snapshot(Instant.parse("2026-01-15T00:00:00Z"), "1d", "70000", "42", "-15", "850")));

        List<AnalysisComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.LONG_TERM);

        assertThat(facts).hasSize(6);
        assertThat(facts).extracting(AnalysisComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.D30,
                                 AnalysisComparisonReference.D90,
                                 AnalysisComparisonReference.D180,
                                 AnalysisComparisonReference.Y52_HIGH,
                                 AnalysisComparisonReference.Y52_LOW,
                                 AnalysisComparisonReference.PREV_LONG_REPORT
                         );
        assertThat(facts.get(0).priceChangeRate()).isEqualByComparingTo("18.2432");
        assertThat(facts.get(1).rsiDelta()).isEqualByComparingTo("22");
        assertThat(facts.get(2).atrChangeRate()).isEqualByComparingTo("114.2857");
        assertThat(facts.get(3).referenceTime()).isEqualTo(Instant.parse("2025-11-20T00:00:00Z"));
        assertThat(facts.get(3).priceChangeRate()).isEqualByComparingTo("-4.8913");
        assertThat(facts.get(4).referenceTime()).isEqualTo(Instant.parse("2025-05-15T00:00:00Z"));
        assertThat(facts.get(4).priceChangeRate()).isEqualByComparingTo("68.2692");
        assertThat(facts.get(5).referenceTime()).isEqualTo(Instant.parse("2026-01-15T00:00:00Z"));
        assertThat(facts.get(5).macdHistogramDelta()).isEqualByComparingTo("35");
    }

    @Test
    void buildFactsSkipsPreviousReportReferenceWhenBasisSnapshotCannotBeLoaded() {
        AnalysisComparisonService service = new AnalysisComparisonService(
                marketIndicatorSnapshotRepository,
                analysisReportRepository
        );

        MarketIndicatorSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:59Z"),
                "4h",
                "87500",
                "62",
                "20",
                "1500"
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(previousReport(
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-01T20:59:59Z")
        )));
        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                "4h",
                Instant.parse("2026-03-01T20:59:59Z")
        )).thenReturn(Optional.empty());

        List<AnalysisComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.MID_TERM);

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

    private AnalysisReportEntity previousReport(AnalysisReportType reportType, Instant analysisBasisTime) {
        return AnalysisReportEntity.builder()
                                   .symbol("BTCUSDT")
                                   .reportType(reportType)
                                   .analysisBasisTime(analysisBasisTime)
                                   .rawReferenceTime(analysisBasisTime.minusSeconds(30))
                                   .sourceDataVersion("basis-key-" + analysisBasisTime)
                                   .analysisEngineVersion("gpt-5.4")
                                   .reportPayload("{\"summary\":\"previous\"}")
                                   .storedTime(analysisBasisTime.plusSeconds(60))
                                   .build();
    }
}

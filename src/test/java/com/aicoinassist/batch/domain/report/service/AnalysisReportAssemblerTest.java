package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerTest {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleBuildsBullishPayloadFromSnapshotFacts() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts()
        );

        assertThat(payload.summary()).contains("SHORT_TERM view");
        assertThat(payload.summary()).contains("bullish");
        assertThat(payload.summary()).contains("Since the previous batch");
        assertThat(payload.marketContext()).contains("above MA20");
        assertThat(payload.marketContext()).contains("D1 price");
        assertThat(payload.comparisonFacts()).hasSize(2);
        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.PREV_BATCH,
                                                         AnalysisComparisonReference.D1
                                                 );
        assertThat(payload.supportLevels()).extracting("label").contains("MA20", "MA60");
        assertThat(payload.resistanceLevels()).extracting("label").contains("BB_UPPER");
        assertThat(payload.scenarios()).extracting("bias").contains("bullish", "neutral");
    }

    @Test
    void assembleAddsRiskFactorsWhenSnapshotShowsExtensionAndVolatility() {
        AnalysisReportPayload payload = assembler.assemble(
                extendedSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts()
        );

        assertThat(payload.riskFactors()).extracting("title")
                                         .contains("RSI overheating", "Band extension", "Elevated volatility");
    }

    @Test
    void assembleBuildsLongTermHighlightsFromCycleReferences() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.LONG_TERM,
                longTermComparisonFacts()
        );

        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.Y52_HIGH,
                                                         AnalysisComparisonReference.Y52_LOW,
                                                         AnalysisComparisonReference.PREV_LONG_REPORT,
                                                         AnalysisComparisonReference.D180
                                                 );
        assertThat(payload.summary()).contains("52-week high");
        assertThat(payload.marketContext()).contains("Highlights:");
        assertThat(payload.marketContext()).contains("cycle floor");
    }

    private MarketIndicatorSnapshotEntity bullishSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-08T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("87500"))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86000"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal("62"))
                                            .macdLine(new BigDecimal("120"))
                                            .macdSignalLine(new BigDecimal("100"))
                                            .macdHistogram(new BigDecimal("20"))
                                            .atr14(new BigDecimal("1500"))
                                            .bollingerUpperBand(new BigDecimal("88500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("85500"))
                                            .build();
    }

    private MarketIndicatorSnapshotEntity extendedSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-08T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("90000"))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86000"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal("74"))
                                            .macdLine(new BigDecimal("150"))
                                            .macdSignalLine(new BigDecimal("110"))
                                            .macdHistogram(new BigDecimal("40"))
                                            .atr14(new BigDecimal("3000"))
                                            .bollingerUpperBand(new BigDecimal("89500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("84500"))
                                            .build();
    }

    private List<AnalysisComparisonFact> comparisonFacts() {
        return List.of(
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.PREV_BATCH,
                        Instant.parse("2026-03-08T23:59:59Z"),
                        new BigDecimal("87000"),
                        new BigDecimal("0.5747"),
                        new BigDecimal("2"),
                        new BigDecimal("5"),
                        new BigDecimal("7.1429")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.D1,
                        Instant.parse("2026-03-08T00:59:59Z"),
                        new BigDecimal("86000"),
                        new BigDecimal("1.7442"),
                        new BigDecimal("7"),
                        new BigDecimal("10"),
                        new BigDecimal("15.3846")
                )
        );
    }

    private List<AnalysisComparisonFact> longTermComparisonFacts() {
        return List.of(
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.D180,
                        Instant.parse("2025-09-10T00:00:00Z"),
                        new BigDecimal("60000"),
                        new BigDecimal("45.8333"),
                        new BigDecimal("27"),
                        new BigDecimal("60"),
                        new BigDecimal("114.2857")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.Y52_HIGH,
                        Instant.parse("2025-11-20T00:00:00Z"),
                        new BigDecimal("92000"),
                        new BigDecimal("-4.8913"),
                        new BigDecimal("-6"),
                        new BigDecimal("-10"),
                        new BigDecimal("53.0612")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.Y52_LOW,
                        Instant.parse("2025-05-15T00:00:00Z"),
                        new BigDecimal("52000"),
                        new BigDecimal("68.2692"),
                        new BigDecimal("34"),
                        new BigDecimal("75"),
                        new BigDecimal("97.3684")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.PREV_LONG_REPORT,
                        Instant.parse("2026-01-15T00:00:00Z"),
                        new BigDecimal("70000"),
                        new BigDecimal("25"),
                        new BigDecimal("20"),
                        new BigDecimal("35"),
                        new BigDecimal("76.4706")
                )
        );
    }
}

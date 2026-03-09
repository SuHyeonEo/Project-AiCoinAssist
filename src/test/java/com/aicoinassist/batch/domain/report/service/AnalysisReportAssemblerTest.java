package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
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
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                shortContinuityNotes()
        );

        assertThat(payload.summary()).contains("SHORT_TERM view");
        assertThat(payload.summary()).contains("bullish");
        assertThat(payload.summary()).contains("Since the previous batch");
        assertThat(payload.marketContext()).contains("above MA20");
        assertThat(payload.marketContext()).contains("D1 price");
        assertThat(payload.marketContext()).contains("Window summary:");
        assertThat(payload.marketContext()).contains("Derivative context:");
        assertThat(payload.marketContext()).contains("Derivative window summary:");
        assertThat(payload.marketContext()).contains("Continuity note:");
        assertThat(payload.derivativeContext()).isNotNull();
        assertThat(payload.derivativeContext().lastFundingRate()).isEqualByComparingTo("0.00045000");
        assertThat(payload.derivativeContext().comparisonFacts()).hasSize(3);
        assertThat(payload.derivativeContext().windowSummaries()).hasSize(2);
        assertThat(payload.comparisonFacts()).hasSize(2);
        assertThat(payload.windowSummaries()).hasSize(2);
        assertThat(payload.windowHighlights()).extracting(AnalysisWindowHighlight::windowType)
                                             .containsExactly(MarketWindowType.LAST_1D, MarketWindowType.LAST_7D);
        assertThat(payload.continuityNotes()).extracting(AnalysisContinuityNote::reference)
                                             .containsExactly(AnalysisComparisonReference.PREV_SHORT_REPORT);
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
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                shortContinuityNotes()
        );

        assertThat(payload.riskFactors()).extracting("title")
                                         .contains("RSI overheating", "Band extension", "Elevated volatility", "Funding skew", "Basis expansion");
    }

    @Test
    void assembleBuildsLongTermHighlightsFromCycleReferences() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.LONG_TERM,
                longTermComparisonFacts(),
                longWindowSummaries(),
                derivativeContext(),
                longContinuityNotes()
        );

        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.Y52_HIGH,
                                                         AnalysisComparisonReference.Y52_LOW,
                                                         AnalysisComparisonReference.D180
                                                 );
        assertThat(payload.summary()).contains("52-week high");
        assertThat(payload.summary()).contains("LAST_52W");
        assertThat(payload.summary()).contains("Previous long-term");
        assertThat(payload.summary()).contains("Derivatives show funding");
        assertThat(payload.marketContext()).contains("Highlights:");
        assertThat(payload.marketContext()).contains("cycle floor");
        assertThat(payload.windowHighlights()).extracting(AnalysisWindowHighlight::windowType)
                                             .containsExactly(MarketWindowType.LAST_180D, MarketWindowType.LAST_52W);
    }

    private List<AnalysisWindowSummary> shortWindowSummaries() {
        return List.of(
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_1D,
                        Instant.parse("2026-03-08T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        24,
                        new BigDecimal("88000"),
                        new BigDecimal("86000"),
                        new BigDecimal("2000"),
                        new BigDecimal("0.75"),
                        new BigDecimal("0.0057"),
                        new BigDecimal("0.0174"),
                        new BigDecimal("100"),
                        new BigDecimal("1400"),
                        new BigDecimal("0.20"),
                        new BigDecimal("0.0714")
                ),
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        168,
                        new BigDecimal("91000"),
                        new BigDecimal("82000"),
                        new BigDecimal("9000"),
                        new BigDecimal("0.61111111"),
                        new BigDecimal("0.03846154"),
                        new BigDecimal("0.06707317"),
                        new BigDecimal("100"),
                        new BigDecimal("1450"),
                        new BigDecimal("0.22"),
                        new BigDecimal("0.03448276")
                )
        );
    }

    private List<AnalysisWindowSummary> longWindowSummaries() {
        return List.of(
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_180D,
                        Instant.parse("2025-09-10T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        180,
                        new BigDecimal("92000"),
                        new BigDecimal("52000"),
                        new BigDecimal("40000"),
                        new BigDecimal("0.8875"),
                        new BigDecimal("0.04891300"),
                        new BigDecimal("0.68269230"),
                        new BigDecimal("120"),
                        new BigDecimal("1300"),
                        new BigDecimal("0.15"),
                        new BigDecimal("0.1538")
                ),
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_52W,
                        Instant.parse("2025-03-10T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        364,
                        new BigDecimal("92000"),
                        new BigDecimal("52000"),
                        new BigDecimal("40000"),
                        new BigDecimal("0.8875"),
                        new BigDecimal("0.04891300"),
                        new BigDecimal("0.68269230"),
                        new BigDecimal("140"),
                        new BigDecimal("1350"),
                        new BigDecimal("0.12"),
                        new BigDecimal("0.1111")
                )
        );
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
                )
        );
    }

    private List<AnalysisContinuityNote> shortContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_SHORT_REPORT,
                Instant.parse("2026-03-08T23:59:59Z"),
                "Previous short-term report highlighted a momentum continuation setup."
        ));
    }

    private List<AnalysisContinuityNote> longContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_LONG_REPORT,
                Instant.parse("2026-01-15T00:00:00Z"),
                "Previous long-term report emphasized cycle recovery above the major base."
        ));
    }

    private AnalysisDerivativeContext derivativeContext() {
        return new AnalysisDerivativeContext(
                Instant.parse("2026-03-09T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:00Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "openInterestSourceEventTime=2026-03-09T00:59:00Z;premiumIndexSourceEventTime=2026-03-09T00:59:30Z;nextFundingTime=2026-03-09T08:00:00Z",
                new BigDecimal("12345.67890000"),
                new BigDecimal("87500.12000000"),
                new BigDecimal("87480.02000000"),
                new BigDecimal("0.00045000"),
                Instant.parse("2026-03-09T08:00:00Z"),
                new BigDecimal("0.12000000"),
                derivativeComparisonFacts(),
                derivativeWindowSummaries()
        );
    }

    private List<AnalysisDerivativeComparisonFact> derivativeComparisonFacts() {
        return List.of(
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.PREV_BATCH,
                        Instant.parse("2026-03-08T23:59:30Z"),
                        new BigDecimal("12000.00000000"),
                        new BigDecimal("0.02880658"),
                        new BigDecimal("0.00010000"),
                        new BigDecimal("0.02000000")
                ),
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.D1,
                        Instant.parse("2026-03-08T00:59:30Z"),
                        new BigDecimal("11800.00000000"),
                        new BigDecimal("0.04624400"),
                        new BigDecimal("0.00014000"),
                        new BigDecimal("0.03500000")
                ),
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.D180,
                        Instant.parse("2025-09-10T00:59:30Z"),
                        new BigDecimal("9000.00000000"),
                        new BigDecimal("0.37174210"),
                        new BigDecimal("0.00030000"),
                        new BigDecimal("0.08000000")
                )
        );
    }

    private List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries() {
        return List.of(
                new AnalysisDerivativeWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        42,
                        new BigDecimal("11000.00000000"),
                        new BigDecimal("0.12233445"),
                        new BigDecimal("0.00025000"),
                        new BigDecimal("0.80000000"),
                        new BigDecimal("0.07000000"),
                        new BigDecimal("0.71428571")
                ),
                new AnalysisDerivativeWindowSummary(
                        MarketWindowType.LAST_180D,
                        Instant.parse("2025-09-10T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        180,
                        new BigDecimal("9800.00000000"),
                        new BigDecimal("0.25976315"),
                        new BigDecimal("0.00018000"),
                        new BigDecimal("1.50000000"),
                        new BigDecimal("0.04000000"),
                        new BigDecimal("2.00000000")
                )
        );
    }
}

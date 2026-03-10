package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisMacroHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOnchainHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisSentimentHighlightImportance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

abstract class AnalysisReportServiceFixtures {

    protected List<AnalysisWindowSummary> shortWindowSummaries() {
        return List.of(
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_1D,
                        Instant.parse("2026-03-08T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        24,
                        new BigDecimal("88200"),
                        new BigDecimal("86000"),
                        new BigDecimal("2200"),
                        new BigDecimal("0.68181818"),
                        new BigDecimal("0.00793651"),
                        new BigDecimal("0.02906977"),
                        new BigDecimal("95"),
                        new BigDecimal("1480"),
                        new BigDecimal("0.18"),
                        new BigDecimal("0.01351351")
                ),
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        168,
                        new BigDecimal("90000"),
                        new BigDecimal("82000"),
                        new BigDecimal("8000"),
                        new BigDecimal("0.68750000"),
                        new BigDecimal("0.02777778"),
                        new BigDecimal("0.06707317"),
                        new BigDecimal("100"),
                        new BigDecimal("1450"),
                        new BigDecimal("0.22"),
                        new BigDecimal("0.03448276")
                )
        );
    }

    protected List<AnalysisWindowSummary> longWindowSummaries() {
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

    protected MarketIndicatorSnapshotEntity bullishSnapshot() {
        return snapshot("1h");
    }

    protected MarketIndicatorSnapshotEntity snapshot(String intervalValue) {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue(intervalValue)
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

    protected MarketIndicatorSnapshotEntity extendedSnapshot() {
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

    protected List<AnalysisComparisonFact> comparisonFacts() {
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

    protected List<AnalysisComparisonFact> longTermComparisonFacts() {
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

    protected List<AnalysisContinuityNote> shortContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_SHORT_REPORT,
                Instant.parse("2026-03-08T23:59:59Z"),
                "Previous short-term report highlighted a momentum continuation setup."
        ));
    }

    protected List<AnalysisContinuityNote> longContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_LONG_REPORT,
                Instant.parse("2026-01-15T00:00:00Z"),
                "Previous long-term report emphasized cycle recovery above the major base."
        ));
    }

    protected AnalysisDerivativeContext derivativeContext() {
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
                derivativeWindowSummaries(),
                List.of()
        );
    }

    protected AnalysisSentimentContext sentimentContext() {
        return new AnalysisSentimentContext(
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                "metricType=FEAR_GREED_INDEX;sourceEventTime=2026-03-09T00:00:00Z",
                new BigDecimal("72.00000000"),
                "Greed",
                3600L,
                sentimentComparisonFacts(),
                sentimentWindowSummaries(),
                List.of()
        );
    }

    protected AnalysisMacroContext macroContext() {
        return new AnalysisMacroContext(
                Instant.parse("2026-03-09T00:00:00Z"),
                "dxyProxyDate=2026-03-09;us10yYieldDate=2026-03-09;usdKrwDate=2026-03-09",
                java.time.LocalDate.parse("2026-03-09"),
                java.time.LocalDate.parse("2026-03-09"),
                java.time.LocalDate.parse("2026-03-09"),
                new BigDecimal("119.84210000"),
                new BigDecimal("4.12000000"),
                new BigDecimal("1453.22000000"),
                macroComparisonFacts(),
                macroWindowSummaries(),
                List.of(
                        new AnalysisMacroHighlight(
                                "Dollar strength regime",
                                "D30 keeps DXY proxy +1.50%, which can pressure crypto risk appetite.",
                                AnalysisMacroHighlightImportance.HIGH,
                                AnalysisComparisonReference.D30
                        )
                )
        );
    }

    protected AnalysisOnchainContext onchainContext() {
        return new AnalysisOnchainContext(
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                "activeAddressSourceEventTime=2026-03-09T00:00:00Z;transactionCountSourceEventTime=2026-03-09T00:00:00Z;marketCapSourceEventTime=2026-03-09T00:00:00Z",
                new BigDecimal("1050000.00000000"),
                new BigDecimal("525000.00000000"),
                new BigDecimal("1700000000000.00000000"),
                onchainComparisonFacts(),
                onchainWindowSummaries(),
                List.of(
                        new AnalysisOnchainHighlight(
                                "D7 activity expansion",
                                "D7 keeps active addresses +10.53%, transactions +9.38%, market cap +9.68%.",
                                AnalysisOnchainHighlightImportance.MEDIUM,
                                AnalysisComparisonReference.D7
                        )
                )
        );
    }

    protected List<AnalysisDerivativeComparisonFact> derivativeComparisonFacts() {
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

    protected List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries() {
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

    protected List<AnalysisSentimentComparisonFact> sentimentComparisonFacts() {
        return List.of(
                new AnalysisSentimentComparisonFact(
                        AnalysisComparisonReference.PREV_BATCH,
                        Instant.parse("2026-03-08T00:00:00Z"),
                        new BigDecimal("68.00000000"),
                        "Neutral",
                        new BigDecimal("4.00000000"),
                        new BigDecimal("0.05882353"),
                        true
                ),
                new AnalysisSentimentComparisonFact(
                        AnalysisComparisonReference.D7,
                        Instant.parse("2026-03-02T00:00:00Z"),
                        new BigDecimal("55.00000000"),
                        "Neutral",
                        new BigDecimal("17.00000000"),
                        new BigDecimal("0.30909091"),
                        true
                ),
                new AnalysisSentimentComparisonFact(
                        AnalysisComparisonReference.D30,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        new BigDecimal("38.00000000"),
                        "Fear",
                        new BigDecimal("34.00000000"),
                        new BigDecimal("0.89473684"),
                        true
                )
        );
    }

    protected List<AnalysisSentimentHighlight> sentimentHighlights() {
        return List.of(
                new AnalysisSentimentHighlight(
                        "Greed regime",
                        "Fear & Greed is at 72 (Greed), which points to risk appetite staying elevated.",
                        AnalysisSentimentHighlightImportance.HIGH,
                        null
                ),
                new AnalysisSentimentHighlight(
                        "PREV_BATCH sentiment shift",
                        "PREV_BATCH changes Fear & Greed by 4 (5.882353%) with classification switching from Neutral to Greed.",
                        AnalysisSentimentHighlightImportance.HIGH,
                        AnalysisComparisonReference.PREV_BATCH
                )
        );
    }

    protected List<AnalysisSentimentWindowSummary> sentimentWindowSummaries() {
        return List.of(
                new AnalysisSentimentWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:00:00Z"),
                        Instant.parse("2026-03-09T00:00:00Z"),
                        7,
                        new BigDecimal("61.00000000"),
                        new BigDecimal("0.18032787"),
                        5,
                        0
                ),
                new AnalysisSentimentWindowSummary(
                        MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        Instant.parse("2026-03-09T00:00:00Z"),
                        30,
                        new BigDecimal("52.00000000"),
                        new BigDecimal("0.38461538"),
                        12,
                        6
                )
        );
    }

    protected List<AnalysisMacroComparisonFact> macroComparisonFacts() {
        return List.of(
                new AnalysisMacroComparisonFact(
                        AnalysisComparisonReference.D30,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        new BigDecimal("118.07000000"),
                        new BigDecimal("3.96000000"),
                        new BigDecimal("1420.00000000"),
                        new BigDecimal("1.77210000"),
                        new BigDecimal("0.01500889"),
                        new BigDecimal("0.16000000"),
                        new BigDecimal("0.04040404"),
                        new BigDecimal("33.22000000"),
                        new BigDecimal("0.02339437")
                )
        );
    }

    protected List<AnalysisMacroWindowSummary> macroWindowSummaries() {
        return List.of(
                new AnalysisMacroWindowSummary(
                        MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        Instant.parse("2026-03-09T00:00:00Z"),
                        30,
                        new BigDecimal("118.90000000"),
                        new BigDecimal("0.00708242"),
                        new BigDecimal("4.00000000"),
                        new BigDecimal("0.03000000"),
                        new BigDecimal("1430.00000000"),
                        new BigDecimal("0.01623776")
                )
        );
    }

    protected List<AnalysisOnchainComparisonFact> onchainComparisonFacts() {
        return List.of(
                new AnalysisOnchainComparisonFact(
                        AnalysisComparisonReference.D7,
                        Instant.parse("2026-03-02T00:00:00Z"),
                        new BigDecimal("950000.00000000"),
                        new BigDecimal("0.10526316"),
                        new BigDecimal("480000.00000000"),
                        new BigDecimal("0.09375000"),
                        new BigDecimal("1550000000000.00000000"),
                        new BigDecimal("0.09677419")
                ),
                new AnalysisOnchainComparisonFact(
                        AnalysisComparisonReference.D30,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        new BigDecimal("1200000.00000000"),
                        new BigDecimal("-0.12500000"),
                        new BigDecimal("610000.00000000"),
                        new BigDecimal("-0.13934426"),
                        new BigDecimal("1820000000000.00000000"),
                        new BigDecimal("-0.06593407")
                )
        );
    }

    protected List<AnalysisOnchainWindowSummary> onchainWindowSummaries() {
        return List.of(
                new AnalysisOnchainWindowSummary(
                        MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:00:00Z"),
                        Instant.parse("2026-03-09T00:00:00Z"),
                        30,
                        new BigDecimal("980000.00000000"),
                        new BigDecimal("0.07142857"),
                        new BigDecimal("500000.00000000"),
                        new BigDecimal("0.05000000"),
                        new BigDecimal("1650000000000.00000000"),
                        new BigDecimal("0.03030303")
                )
        );
    }

    protected List<AnalysisPriceLevel> supportLevels() {
        return List.of(
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.MA20,
                        AnalysisPriceLevelSourceType.MOVING_AVERAGE,
                        new BigDecimal("87000"),
                        new BigDecimal("0.00571429"),
                        new BigDecimal("0.64428571"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        2,
                        1,
                        "Short-term average support",
                        List.of("Current price 87500 vs MA20 87000", "SUPPORT distance 0.57%")
                ),
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.MA60,
                        AnalysisPriceLevelSourceType.MOVING_AVERAGE,
                        new BigDecimal("86000"),
                        new BigDecimal("0.01714286"),
                        new BigDecimal("0.78285714"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        3,
                        2,
                        "Mid-trend average support",
                        List.of("Current price 87500 vs MA60 86000", "SUPPORT distance 1.71%")
                )
        );
    }

    protected List<AnalysisPriceLevel> resistanceLevels() {
        return List.of(
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.BB_UPPER,
                        AnalysisPriceLevelSourceType.BOLLINGER_BAND,
                        new BigDecimal("88500"),
                        new BigDecimal("0.01142857"),
                        new BigDecimal("0.63857143"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        1,
                        1,
                        "Upper Bollinger band resistance",
                        List.of("Current price 87500 vs BB_UPPER 88500", "RESISTANCE distance 1.14%")
                )
        );
    }

    protected List<AnalysisPriceZone> supportZones() {
        return List.of(
                new AnalysisPriceZone(
                        AnalysisPriceZoneType.SUPPORT,
                        1,
                        new BigDecimal("86850"),
                        new BigDecimal("86850"),
                        new BigDecimal("87000"),
                        new BigDecimal("0.00742857"),
                        new BigDecimal("0.00514286"),
                        new BigDecimal("0.89285714"),
                        AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                        AnalysisPriceLevelLabel.PIVOT_LOW,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        5,
                        4,
                        1,
                        List.of(AnalysisPriceLevelLabel.MA20, AnalysisPriceLevelLabel.PIVOT_LOW),
                        List.of(AnalysisPriceLevelSourceType.MOVING_AVERAGE, AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("SUPPORT zone spans 86850 to 87000 with 2 candidate levels.", "Recent tests=5, rejections=4, breaks=1 within 14 days.")
                )
        );
    }

    protected List<AnalysisPriceZone> resistanceZones() {
        return List.of(
                new AnalysisPriceZone(
                        AnalysisPriceZoneType.RESISTANCE,
                        1,
                        new BigDecimal("88560"),
                        new BigDecimal("88500"),
                        new BigDecimal("88620"),
                        new BigDecimal("0.01211429"),
                        new BigDecimal("0.01165714"),
                        new BigDecimal("0.86285714"),
                        AnalysisPriceZoneInteractionType.BELOW_ZONE,
                        AnalysisPriceLevelLabel.PIVOT_HIGH,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        3,
                        2,
                        0,
                        List.of(AnalysisPriceLevelLabel.BB_UPPER, AnalysisPriceLevelLabel.PIVOT_HIGH),
                        List.of(AnalysisPriceLevelSourceType.BOLLINGER_BAND, AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("RESISTANCE zone spans 88500 to 88620 with 2 candidate levels.", "Recent tests=3, rejections=2, breaks=0 within 14 days.")
                )
        );
    }

    protected AnalysisLevelContextPayload levelContext() {
        return new AnalysisLevelContextPayload(
                supportZones().get(0),
                resistanceZones().get(0),
                List.of(
                        new AnalysisZoneInteractionFact(
                                AnalysisPriceZoneType.SUPPORT,
                                1,
                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                "Nearest support zone is 86850 to 87000, currently above zone with 5 tests and break risk 18%.",
                                supportZones().get(0).triggerFacts()
                        ),
                        new AnalysisZoneInteractionFact(
                                AnalysisPriceZoneType.RESISTANCE,
                                1,
                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                "Nearest resistance zone is 88500 to 88620, currently below zone with 3 tests and break risk 5%.",
                                resistanceZones().get(0).triggerFacts()
                        )
                ),
                new BigDecimal("0.18000000"),
                new BigDecimal("0.05000000"),
                List.of(
                        new AnalysisLevelContextComparisonFact(
                                AnalysisComparisonReference.PREV_BATCH,
                                Instant.parse("2026-03-08T23:59:59Z"),
                                new BigDecimal("0.00288684"),
                                new BigDecimal("0.00158371"),
                                new BigDecimal("0.04285714"),
                                new BigDecimal("0.03285714"),
                                new BigDecimal("0.04000000"),
                                new BigDecimal("-0.02000000"),
                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                AnalysisPriceZoneInteractionType.INSIDE_ZONE,
                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                AnalysisPriceZoneInteractionType.BELOW_ZONE
                        ),
                        new AnalysisLevelContextComparisonFact(
                                AnalysisComparisonReference.D1,
                                Instant.parse("2026-03-08T00:59:59Z"),
                                new BigDecimal("0.00988372"),
                                new BigDecimal("-0.00492611"),
                                new BigDecimal("0.08285714"),
                                new BigDecimal("-0.03714286"),
                                new BigDecimal("0.08000000"),
                                new BigDecimal("-0.04000000"),
                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                AnalysisPriceZoneInteractionType.INSIDE_ZONE
                        )
                ),
                List.of()
        );
    }

    protected MarketCandidateLevelSnapshotEntity candidateLevelEntity(
            String levelType,
            String levelLabel,
            String sourceType,
            String levelPrice,
            String distanceFromCurrent,
            String strengthScore,
            String rationale,
            String triggerFactsPayload
    ) {
        return MarketCandidateLevelSnapshotEntity.builder()
                                                 .symbol("BTCUSDT")
                                                 .intervalValue("4h")
                                                 .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                 .referenceTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                 .levelType(levelType)
                                                 .levelLabel(levelLabel)
                                                 .sourceType(sourceType)
                                                 .currentPrice(new BigDecimal("87500"))
                                                 .levelPrice(new BigDecimal(levelPrice))
                                                 .distanceFromCurrent(new BigDecimal(distanceFromCurrent))
                                                 .strengthScore(new BigDecimal(strengthScore))
                                                 .reactionCount(2)
                                                 .clusterSize(1)
                                                 .rationale(rationale)
                                                 .triggerFactsPayload(triggerFactsPayload)
                                                 .sourceDataVersion("basis-key;" + levelType + ";" + levelLabel)
                                                 .build();
    }

    protected MarketCandidateLevelZoneSnapshotEntity candidateLevelZoneEntity(
            String zoneType,
            Integer zoneRank,
            String representativePrice,
            String zoneLow,
            String zoneHigh,
            String distanceFromCurrent,
            String distanceToZone,
            String zoneStrengthScore,
            String interactionType,
            String strongestLevelLabel,
            String strongestSourceType,
            String includedLevelLabelsPayload,
            String includedSourceTypesPayload
    ) {
        return MarketCandidateLevelZoneSnapshotEntity.builder()
                                                     .symbol("BTCUSDT")
                                                     .intervalValue("4h")
                                                     .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                     .zoneType(zoneType)
                                                     .zoneRank(zoneRank)
                                                     .currentPrice(new BigDecimal("87500"))
                                                     .representativePrice(new BigDecimal(representativePrice))
                                                     .zoneLow(new BigDecimal(zoneLow))
                                                     .zoneHigh(new BigDecimal(zoneHigh))
                                                     .distanceFromCurrent(new BigDecimal(distanceFromCurrent))
                                                     .distanceToZone(new BigDecimal(distanceToZone))
                                                     .zoneStrengthScore(new BigDecimal(zoneStrengthScore))
                                                     .interactionType(interactionType)
                                                     .strongestLevelLabel(strongestLevelLabel)
                                                     .strongestSourceType(strongestSourceType)
                                                     .levelCount(2)
                                                     .recentTestCount(4)
                                                     .recentRejectionCount(3)
                                                     .recentBreakCount(1)
                                                     .includedLevelLabelsPayload(includedLevelLabelsPayload)
                                                     .includedSourceTypesPayload(includedSourceTypesPayload)
                                                     .triggerFactsPayload("[\"" + zoneType + " zone spans " + zoneLow + " to " + zoneHigh + " with 2 candidate levels.\"]")
                                                     .sourceDataVersion("basis-key;" + zoneType + ";rank=" + zoneRank)
                                                     .build();
    }
}

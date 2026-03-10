package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFactSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryKeyMessagePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportPersistenceServiceTest {

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder()
                                                        .findAndAddModules()
                                                        .build();

    @Test
    void saveRefreshesExistingReportWhenIdentityMatches() throws Exception {
        AnalysisReportPersistenceService service = new AnalysisReportPersistenceService(analysisReportRepository, objectMapper);

        AnalysisReportDraft draft = draft(
                payload("Refreshed summary"),
                Instant.parse("2026-03-09T01:00:30Z")
        );

        AnalysisReportEntity existingEntity = AnalysisReportEntity.builder()
                                                                  .symbol("BTCUSDT")
                                                                  .reportType(AnalysisReportType.SHORT_TERM)
                                                                  .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                                  .rawReferenceTime(Instant.parse("2026-03-09T00:59:00Z"))
                                                                  .sourceDataVersion("snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z")
                                                                  .analysisEngineVersion("gpt-5.4")
                                                                  .reportPayload("{\"summary\":\"old\"}")
                                                                  .storedTime(Instant.parse("2026-03-09T01:00:00Z"))
                                                                  .build();

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeAndSourceDataVersionAndAnalysisEngineVersionOrderByIdDesc(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4"
        )).thenReturn(Optional.of(existingEntity));

        AnalysisReportEntity result = service.save(draft);

        verify(analysisReportRepository, never()).save(any(AnalysisReportEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getRawReferenceTime()).isEqualTo(Instant.parse("2026-03-09T00:59:30Z"));
        assertThat(existingEntity.getReportPayload()).isEqualTo(objectMapper.writeValueAsString(payload("Refreshed summary")));
        assertThat(existingEntity.getStoredTime()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
    }

    @Test
    void savePersistsNewReportWhenIdentityDoesNotExist() throws Exception {
        AnalysisReportPersistenceService service = new AnalysisReportPersistenceService(analysisReportRepository, objectMapper);
        AnalysisReportDraft draft = draft(
                payload("New summary"),
                Instant.parse("2026-03-09T01:00:30Z")
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeAndSourceDataVersionAndAnalysisEngineVersionOrderByIdDesc(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4"
        )).thenReturn(Optional.empty());
        when(analysisReportRepository.save(any(AnalysisReportEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisReportEntity result = service.save(draft);

        assertThat(result.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(result.getReportType()).isEqualTo(AnalysisReportType.SHORT_TERM);
        assertThat(result.getAnalysisBasisTime()).isEqualTo(Instant.parse("2026-03-09T00:59:59Z"));
        assertThat(result.getRawReferenceTime()).isEqualTo(Instant.parse("2026-03-09T00:59:30Z"));
        assertThat(result.getSourceDataVersion()).isEqualTo(
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z"
        );
        assertThat(result.getAnalysisEngineVersion()).isEqualTo("gpt-5.4");
        assertThat(result.getReportPayload()).isEqualTo(objectMapper.writeValueAsString(payload("New summary")));
        assertThat(result.getStoredTime()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
    }

    private AnalysisReportDraft draft(AnalysisReportPayload reportPayload, Instant storedTime) {
        return new AnalysisReportDraft(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                reportPayload,
                storedTime
        );
    }

    private AnalysisReportPayload payload(String summary) {
        return new AnalysisReportPayload(
                new AnalysisSummaryPayload(
                        "SHORT_TERM view",
                        AnalysisOutlookType.CONSTRUCTIVE,
                        AnalysisConfidenceLevel.HIGH,
                        new AnalysisSummaryKeyMessagePayload(
                                summary,
                                List.of("signal detail"),
                                "continuity"
                        ),
                        List.of(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.COMPARISON, "PREV_BATCH comparison", "detail", AnalysisContextHeadlineImportance.HIGH),
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.WINDOW, "LAST_7D position", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.DERIVATIVE, "D1 derivative shift", "detail", AnalysisContextHeadlineImportance.MEDIUM)
                        )
                ),
                new AnalysisMarketContextPayload(
                        new AnalysisCurrentStatePayload(
                                new BigDecimal("87500.00"),
                                AnalysisTrendLabel.BULLISH,
                                AnalysisVolatilityLabel.MODERATE,
                                AnalysisRangePositionLabel.UPPER_RANGE,
                                List.of(
                                        new AnalysisMovingAveragePositionPayload("MA20", new BigDecimal("87000.00"), true),
                                        new AnalysisMovingAveragePositionPayload("MA60", new BigDecimal("86000.00"), true),
                                        new AnalysisMovingAveragePositionPayload("MA120", new BigDecimal("85000.00"), true)
                                ),
                                new AnalysisMomentumStatePayload(
                                        new BigDecimal("62"),
                                        new BigDecimal("20"),
                                        "RSI14 62, MACD histogram 20"
                                )
                        ),
                        new AnalysisComparisonContextPayload(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.COMPARISON, "PREV_BATCH comparison", "detail", AnalysisContextHeadlineImportance.HIGH),
                                new AnalysisComparisonFactSummaryPayload(
                                        "PREV_BATCH price +0.5747%, RSI Δ +2, MACD hist Δ +5.",
                                        List.of("D1 price +1.7442%, RSI Δ +7, MACD hist Δ +10.")
                                ),
                                List.of("PREV_BATCH confirms the latest impulse with MACD histogram Δ +5.")
                        ),
                        new AnalysisWindowContextPayload(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.WINDOW, "LAST_7D position", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisWindowContextSummaryPayload(
                                        "LAST_7D range 82000 to 90000.",
                                        "LAST_7D price is at 68.75% of the range.",
                                        "ATR vs average +3.45%."
                                ),
                                List.of("LAST_7D volume vs average +22%, ATR vs average +3.45%, distance from range high 2.78%.")
                        ),
                        new AnalysisLevelContextPayload(
                                null,
                                null,
                                List.of(),
                                new BigDecimal("0.18000000"),
                                new BigDecimal("0.05000000"),
                                List.of(
                                        new AnalysisLevelContextComparisonFact(
                                                AnalysisComparisonReference.PREV_BATCH,
                                                Instant.parse("2026-03-08T23:59:59Z"),
                                                new BigDecimal("0.00250000"),
                                                new BigDecimal("-0.00150000"),
                                                new BigDecimal("0.04000000"),
                                                new BigDecimal("-0.01000000"),
                                                new BigDecimal("0.02000000"),
                                                new BigDecimal("-0.01000000"),
                                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                                AnalysisPriceZoneInteractionType.INSIDE_ZONE,
                                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                                AnalysisPriceZoneInteractionType.BELOW_ZONE
                                        )
                                ),
                                List.of(
                                        new AnalysisLevelContextHighlight(
                                                AnalysisComparisonReference.PREV_BATCH,
                                                "PREV_BATCH level context",
                                                "PREV_BATCH keeps support strength stronger while resistance stays capped."
                                        )
                                )
                        ),
                        new AnalysisDerivativeContextSummaryPayload(
                                "Funding +0.045%, basis +0.12%.",
                                "LAST_7D OI vs average +12.23%, funding vs average +80.00%, basis vs average +71.43%.",
                                List.of("D1 keeps OI +4.6244%, funding Δ +0.014%, basis Δ +0.035%."),
                                List.of("Macro volatility"),
                                7L
                        ),
                        new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.DERIVATIVE, "D1 derivative shift", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                        new AnalysisContinuityContextPayload(
                                AnalysisComparisonReference.PREV_SHORT_REPORT,
                                "Previous short-term report highlighted momentum continuation.",
                                List.of("Previous short-term report highlighted momentum continuation."),
                                List.of()
                        )
                ),
                List.of(
                        new AnalysisComparisonFact(
                                AnalysisComparisonReference.PREV_BATCH,
                                Instant.parse("2026-03-08T23:59:59Z"),
                                new BigDecimal("87000.00"),
                                new BigDecimal("0.5747"),
                                new BigDecimal("2.00"),
                                new BigDecimal("5.00"),
                                new BigDecimal("7.1429")
                        )
                ),
                List.of(
                        new AnalysisComparisonHighlight(
                                AnalysisComparisonReference.PREV_BATCH,
                                "Since the previous batch, price changed +0.5747% and RSI14 moved +2.",
                                "PREV_BATCH confirms the latest impulse with MACD histogram Δ +5."
                        )
                ),
                List.of(
                        new AnalysisWindowHighlight(
                                MarketWindowType.LAST_7D,
                                "LAST_7D keeps price at 68.75% of the range.",
                                "LAST_7D volume vs average +22%, ATR vs average +3.45%, distance from range high 2.78%."
                        )
                ),
                List.of(
                        new AnalysisContinuityNote(
                                AnalysisComparisonReference.PREV_SHORT_REPORT,
                                Instant.parse("2026-03-08T23:59:59Z"),
                                "Previous short-term report highlighted momentum continuation."
                        )
                ),
                List.of(
                        new AnalysisWindowSummary(
                                MarketWindowType.LAST_7D,
                                Instant.parse("2026-03-02T00:59:59Z"),
                                Instant.parse("2026-03-09T00:59:59Z"),
                                168,
                                new BigDecimal("90000.00"),
                                new BigDecimal("82000.00"),
                                new BigDecimal("8000.00"),
                                new BigDecimal("0.68750000"),
                                new BigDecimal("0.02777778"),
                                new BigDecimal("0.06707317"),
                                new BigDecimal("100.00000000"),
                                new BigDecimal("1450.00000000"),
                                new BigDecimal("0.22000000"),
                                new BigDecimal("0.03448276")
                        )
                ),
                new AnalysisDerivativeContext(
                        Instant.parse("2026-03-09T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:00Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        "context-basis-key",
                        new BigDecimal("12345.67890000"),
                        new BigDecimal("87500.12000000"),
                        new BigDecimal("87480.02000000"),
                        new BigDecimal("0.00045000"),
                        Instant.parse("2026-03-09T08:00:00Z"),
                        new BigDecimal("0.12000000"),
                        List.of(
                                new AnalysisDerivativeComparisonFact(
                                        AnalysisComparisonReference.D1,
                                        Instant.parse("2026-03-08T00:59:30Z"),
                                        new BigDecimal("11800.00000000"),
                                        new BigDecimal("0.04624400"),
                                        new BigDecimal("0.00014000"),
                                        new BigDecimal("0.03500000")
                                )
                        ),
                        List.of(
                                new AnalysisDerivativeWindowSummary(
                                        MarketWindowType.LAST_7D,
                                        Instant.parse("2026-03-02T00:59:30Z"),
                                        Instant.parse("2026-03-09T00:59:30Z"),
                                        168,
                                        new BigDecimal("11000.00000000"),
                                        new BigDecimal("0.12233445"),
                                        new BigDecimal("0.00025000"),
                                        new BigDecimal("0.80000000"),
                                        new BigDecimal("0.07000000"),
                                        new BigDecimal("0.71428571")
                                )
                        ),
                        List.of(
                                new AnalysisDerivativeHighlight(
                                        "D1 derivative shift",
                                        "D1 keeps OI +4.6244%, funding Δ +0.014%, basis Δ +0.035%.",
                                        AnalysisDerivativeHighlightImportance.MEDIUM,
                                        AnalysisDerivativeMetricType.OPEN_INTEREST,
                                        AnalysisComparisonReference.D1,
                                        null
                                )
                        )
                ),
                List.of(new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.S1,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        new BigDecimal("84500.00"),
                        new BigDecimal("0.03428571"),
                        new BigDecimal("0.75"),
                        Instant.parse("2026-03-08T12:00:00Z"),
                        2,
                        1,
                        "Recent pullback low",
                        List.of("Current price is 3.43% away from S1.")
                )),
                List.of(new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.R1,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        new BigDecimal("88500.00"),
                        new BigDecimal("0.01142857"),
                        new BigDecimal("0.75"),
                        Instant.parse("2026-03-08T18:00:00Z"),
                        2,
                        1,
                        "Recent swing high",
                        List.of("Current price is 1.14% away from R1.")
                )),
                List.of(new AnalysisPriceZone(
                        AnalysisPriceZoneType.SUPPORT,
                        1,
                        new BigDecimal("84550.00"),
                        new BigDecimal("84500.00"),
                        new BigDecimal("84600.00"),
                        new BigDecimal("0.03371429"),
                        new BigDecimal("0.02100000"),
                        new BigDecimal("0.84"),
                        AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                        AnalysisPriceLevelLabel.S1,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        4,
                        3,
                        1,
                        List.of(AnalysisPriceLevelLabel.S1, AnalysisPriceLevelLabel.PIVOT_LOW),
                        List.of(AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("SUPPORT zone spans 84500 to 84600 with 2 candidate levels.")
                )),
                List.of(new AnalysisPriceZone(
                        AnalysisPriceZoneType.RESISTANCE,
                        1,
                        new BigDecimal("88550.00"),
                        new BigDecimal("88500.00"),
                        new BigDecimal("88600.00"),
                        new BigDecimal("0.01200000"),
                        new BigDecimal("0.01120000"),
                        new BigDecimal("0.82"),
                        AnalysisPriceZoneInteractionType.BELOW_ZONE,
                        AnalysisPriceLevelLabel.R1,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        3,
                        2,
                        0,
                        List.of(AnalysisPriceLevelLabel.R1, AnalysisPriceLevelLabel.PIVOT_HIGH),
                        List.of(AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("RESISTANCE zone spans 88500 to 88600 with 2 candidate levels.")
                )),
                null,
                null,
                List.of(),
                List.of(new AnalysisRiskFactor(
                        AnalysisRiskFactorType.MACRO_VOLATILITY,
                        "Macro volatility",
                        "USD strength can pressure crypto risk assets.",
                        List.of("USD strength remains a macro headwind.")
                )),
                List.of(new AnalysisScenario(
                        "Base case",
                        AnalysisScenarioBias.BULLISH,
                        List.of("Price consolidates above support."),
                        "Price consolidates above support and retests resistance.",
                        List.of("A support break invalidates the base case.")
                ))
        );
    }
}

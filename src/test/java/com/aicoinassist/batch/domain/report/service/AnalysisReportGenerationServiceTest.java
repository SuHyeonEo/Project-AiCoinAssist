package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportGenerationServiceTest {

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Mock
    private AnalysisComparisonService analysisComparisonService;

    @Mock
    private AnalysisReportAssembler analysisReportAssembler;

    @Mock
    private AnalysisReportPersistenceService analysisReportPersistenceService;

    @Test
    void generateAndSaveBuildsDraftFromLatestMappedSnapshot() {
        AnalysisReportGenerationService service = new AnalysisReportGenerationService(
                marketIndicatorSnapshotRepository,
                analysisComparisonService,
                analysisReportAssembler,
                analysisReportPersistenceService
        );

        MarketIndicatorSnapshotEntity snapshot = snapshot("4h");
        List<AnalysisComparisonFact> comparisonFacts = List.of(
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
        AnalysisReportPayload payload = new AnalysisReportPayload(
                "summary",
                "context",
                comparisonFacts,
                List.of(new AnalysisComparisonHighlight(
                        AnalysisComparisonReference.D1,
                        "D1 shows price +1.7442% versus the reference point.",
                        "D1 keeps RSI Δ +7 and MACD hist Δ +10."
                )),
                List.of(),
                List.of(),
                List.of(),
                List.of(new AnalysisScenario("Base case", "bullish", "description"))
        );
        AnalysisReportEntity savedEntity = AnalysisReportEntity.builder()
                                                               .symbol("BTCUSDT")
                                                               .reportType(AnalysisReportType.MID_TERM)
                                                               .analysisBasisTime(snapshot.getSnapshotTime())
                                                               .rawReferenceTime(snapshot.getPriceSourceEventTime())
                                                               .sourceDataVersion(snapshot.getSourceDataVersion())
                                                               .analysisEngineVersion("gpt-5.4")
                                                               .reportPayload("{\"summary\":\"summary\"}")
                                                               .storedTime(Instant.parse("2026-03-09T01:00:30Z"))
                                                               .build();

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "4h"))
                .thenReturn(Optional.of(snapshot));
        when(analysisComparisonService.buildFacts(snapshot, AnalysisReportType.MID_TERM)).thenReturn(comparisonFacts);
        when(analysisReportAssembler.assemble(snapshot, AnalysisReportType.MID_TERM, comparisonFacts)).thenReturn(payload);
        when(analysisReportPersistenceService.save(org.mockito.ArgumentMatchers.any(AnalysisReportDraft.class)))
                .thenReturn(savedEntity);

        AnalysisReportEntity result = service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        );

        ArgumentCaptor<AnalysisReportDraft> draftCaptor = ArgumentCaptor.forClass(AnalysisReportDraft.class);
        verify(analysisReportPersistenceService).save(draftCaptor.capture());

        AnalysisReportDraft draft = draftCaptor.getValue();
        assertThat(draft.symbol()).isEqualTo("BTCUSDT");
        assertThat(draft.reportType()).isEqualTo(AnalysisReportType.MID_TERM);
        assertThat(draft.analysisBasisTime()).isEqualTo(snapshot.getSnapshotTime());
        assertThat(draft.rawReferenceTime()).isEqualTo(snapshot.getPriceSourceEventTime());
        assertThat(draft.sourceDataVersion()).isEqualTo(snapshot.getSourceDataVersion());
        assertThat(draft.analysisEngineVersion()).isEqualTo("gpt-5.4");
        assertThat(draft.reportPayload()).isSameAs(payload);
        assertThat(draft.storedTime()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
        assertThat(result).isSameAs(savedEntity);
    }

    @Test
    void generateAndSaveFailsWhenNoSnapshotExistsForMappedInterval() {
        AnalysisReportGenerationService service = new AnalysisReportGenerationService(
                marketIndicatorSnapshotRepository,
                analysisComparisonService,
                analysisReportAssembler,
                analysisReportPersistenceService
        );

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "1d"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("symbol=BTCUSDT")
          .hasMessageContaining("interval=1d");
    }

    private MarketIndicatorSnapshotEntity snapshot(String intervalValue) {
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
}

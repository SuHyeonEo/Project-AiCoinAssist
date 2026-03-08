package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
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
                summary,
                "BTC is holding above short-term support while momentum remains constructive.",
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
                List.of(new AnalysisPriceLevel("S1", new BigDecimal("84500.00"), "Recent pullback low")),
                List.of(new AnalysisPriceLevel("R1", new BigDecimal("88500.00"), "Recent swing high")),
                List.of(new AnalysisRiskFactor("Macro volatility", "USD strength can pressure crypto risk assets.")),
                List.of(new AnalysisScenario("Base case", "bullish", "Price consolidates above support and retests resistance."))
        );
    }
}

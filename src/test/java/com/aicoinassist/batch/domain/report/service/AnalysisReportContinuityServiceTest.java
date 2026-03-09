package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportContinuityServiceTest {

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder()
                                                        .findAndAddModules()
                                                        .build();

    @Test
    void buildNotesReturnsNarrativeContinuityFromPreviousReport() {
        AnalysisReportContinuityService service = new AnalysisReportContinuityService(
                analysisReportRepository,
                objectMapper
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(
                AnalysisReportEntity.builder()
                                    .symbol("BTCUSDT")
                                    .reportType(AnalysisReportType.MID_TERM)
                                    .analysisBasisTime(Instant.parse("2026-03-01T20:59:59Z"))
                                    .rawReferenceTime(Instant.parse("2026-03-01T20:59:30Z"))
                                    .sourceDataVersion("basis-key")
                                    .analysisEngineVersion("report-assembler-v1")
                                    .reportPayload("{\"summary\":{\"headline\":\"MID_TERM view\",\"keyMessage\":\"Previous mid-term report emphasized structure holding above weekly support.\"}}")
                                    .storedTime(Instant.parse("2026-03-01T21:00:30Z"))
                                    .build()
        ));

        List<AnalysisContinuityNote> notes = service.buildNotes(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        );

        assertThat(notes).singleElement().satisfies(note -> {
            assertThat(note.reference()).isEqualTo(AnalysisComparisonReference.PREV_MID_REPORT);
            assertThat(note.previousAnalysisBasisTime()).isEqualTo(Instant.parse("2026-03-01T20:59:59Z"));
            assertThat(note.summary()).contains("structure holding above weekly support");
        });
    }

    @Test
    void buildNotesReturnsFallbackWhenPreviousReportPayloadCannotBeParsed() {
        AnalysisReportContinuityService service = new AnalysisReportContinuityService(
                analysisReportRepository,
                objectMapper
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.of(
                AnalysisReportEntity.builder()
                                    .symbol("BTCUSDT")
                                    .reportType(AnalysisReportType.LONG_TERM)
                                    .analysisBasisTime(Instant.parse("2025-12-31T23:59:59Z"))
                                    .rawReferenceTime(Instant.parse("2025-12-31T23:59:30Z"))
                                    .sourceDataVersion("basis-key")
                                    .analysisEngineVersion("report-assembler-v1")
                                    .reportPayload("{broken-json")
                                    .storedTime(Instant.parse("2026-01-01T00:00:30Z"))
                                    .build()
        ));

        List<AnalysisContinuityNote> notes = service.buildNotes(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        );

        assertThat(notes).singleElement().satisfies(note -> {
            assertThat(note.reference()).isEqualTo(AnalysisComparisonReference.PREV_LONG_REPORT);
            assertThat(note.summary()).isEqualTo("Previous report exists but summary could not be parsed.");
        });
    }

    @Test
    void buildNotesReturnsEmptyWhenPreviousReportDoesNotExist() {
        AnalysisReportContinuityService service = new AnalysisReportContinuityService(
                analysisReportRepository,
                objectMapper
        );

        when(analysisReportRepository.findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        )).thenReturn(Optional.empty());

        List<AnalysisContinuityNote> notes = service.buildNotes(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z")
        );

        assertThat(notes).isEmpty();
    }
}

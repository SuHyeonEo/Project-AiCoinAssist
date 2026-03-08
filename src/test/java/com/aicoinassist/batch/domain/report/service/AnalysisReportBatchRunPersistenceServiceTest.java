package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchRunPersistenceServiceTest {

    @Mock
    private AnalysisReportBatchRunRepository analysisReportBatchRunRepository;

    @Mock
    private AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder()
                                                        .findAndAddModules()
                                                        .build();

    @Test
    void savePersistsRunSummaryAndAssetDetailPayloads() throws Exception {
        AnalysisReportBatchRunPersistenceService service = new AnalysisReportBatchRunPersistenceService(
                analysisReportBatchRunRepository,
                analysisReportBatchAssetResultRepository,
                objectMapper
        );

        AnalysisReportBatchRunResult runResult = new AnalysisReportBatchRunResult(
                "run-001",
                Instant.parse("2026-03-09T01:00:00Z"),
                Instant.parse("2026-03-09T01:00:20Z"),
                20000L,
                List.of(
                        new AnalysisReportBatchResult(
                                "run-001",
                                "BTCUSDT",
                                Instant.parse("2026-03-09T01:00:00Z"),
                                Instant.parse("2026-03-09T01:00:05Z"),
                                5000L,
                                List.of(new AnalysisReportSnapshotStepResult(CandleInterval.ONE_HOUR, true, null)),
                                List.of(new AnalysisReportStepResult(AnalysisReportType.SHORT_TERM, true, null)),
                                null
                        ),
                        AnalysisReportBatchResult.crashed(
                                "run-001",
                                "ETHUSDT",
                                Instant.parse("2026-03-09T01:00:06Z"),
                                Instant.parse("2026-03-09T01:00:09Z"),
                                "unexpected crash"
                        )
                )
        );

        when(analysisReportBatchRunRepository.save(any(AnalysisReportBatchRunEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisReportBatchRunEntity savedRun = service.save(
                runResult,
                "report-assembler-v1",
                Instant.parse("2026-03-09T01:00:20Z")
        );

        ArgumentCaptor<List<AnalysisReportBatchAssetResultEntity>> assetCaptor = ArgumentCaptor.forClass(List.class);

        assertThat(savedRun.getRunId()).isEqualTo("run-001");
        assertThat(savedRun.getEngineVersion()).isEqualTo("report-assembler-v1");
        assertThat(savedRun.getAssetSuccessCount()).isEqualTo(1);
        assertThat(savedRun.getAssetFailureCount()).isEqualTo(1);
        assertThat(savedRun.getStoredTime()).isEqualTo(Instant.parse("2026-03-09T01:00:20Z"));

        verify(analysisReportBatchAssetResultRepository).saveAll(assetCaptor.capture());
        assertThat(assetCaptor.getValue()).hasSize(2);

        AnalysisReportBatchAssetResultEntity successEntity = assetCaptor.getValue().get(0);
        assertThat(successEntity.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(successEntity.getSnapshotSuccessCount()).isEqualTo(1);
        assertThat(successEntity.getReportSuccessCount()).isEqualTo(1);
        assertThat(successEntity.getCrashErrorMessage()).isNull();
        assertThat(successEntity.getSnapshotResultsPayload()).isEqualTo(
                objectMapper.writeValueAsString(List.of(new AnalysisReportSnapshotStepResult(CandleInterval.ONE_HOUR, true, null)))
        );
        assertThat(successEntity.getReportResultsPayload()).isEqualTo(
                objectMapper.writeValueAsString(List.of(new AnalysisReportStepResult(AnalysisReportType.SHORT_TERM, true, null)))
        );

        AnalysisReportBatchAssetResultEntity failedEntity = assetCaptor.getValue().get(1);
        assertThat(failedEntity.getSymbol()).isEqualTo("ETHUSDT");
        assertThat(failedEntity.getCrashErrorMessage()).isEqualTo("unexpected crash");
        assertThat(failedEntity.getSnapshotResultsPayload()).isEqualTo("[]");
        assertThat(failedEntity.getReportResultsPayload()).isEqualTo("[]");
    }
}

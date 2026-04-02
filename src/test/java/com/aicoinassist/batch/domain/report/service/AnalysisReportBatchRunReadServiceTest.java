package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunDetailView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunSummaryView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchRunReadServiceTest {

    @Mock
    private AnalysisReportBatchRunRepository analysisReportBatchRunRepository;

    @Mock
    private AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder()
                                                        .findAndAddModules()
                                                        .build();

    @Test
    void listRecentRunsReturnsNewestRunSummariesFirst() {
        AnalysisReportBatchRunReadService service = new AnalysisReportBatchRunReadService(
                analysisReportBatchRunRepository,
                analysisReportBatchAssetResultRepository,
                objectMapper
        );

        when(analysisReportBatchRunRepository.findAllByOrderByStartedAtDesc(any(Pageable.class))).thenReturn(List.of(
                runEntity("run-002", BatchExecutionStatus.PARTIAL_FAILURE, BatchExecutionTriggerType.MANUAL_RERUN, "run-001"),
                runEntity("run-001", BatchExecutionStatus.SUCCESS, BatchExecutionTriggerType.SCHEDULED, null)
        ));

        List<AnalysisReportBatchRunSummaryView> runs = service.listRecentRuns(10);

        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).runId()).isEqualTo("run-002");
        assertThat(runs.get(0).rerunnable()).isTrue();
        assertThat(runs.get(0).rerunSourceRunId()).isEqualTo("run-001");
        assertThat(runs.get(1).runId()).isEqualTo("run-001");
        assertThat(runs.get(1).rerunnable()).isFalse();
    }

    @Test
    void getRunDetailReturnsParsedAssetResultsAndRerunnableSymbols() throws Exception {
        AnalysisReportBatchRunReadService service = new AnalysisReportBatchRunReadService(
                analysisReportBatchRunRepository,
                analysisReportBatchAssetResultRepository,
                objectMapper
        );

        when(analysisReportBatchRunRepository.findByRunId("run-001")).thenReturn(Optional.of(
                runEntity("run-001", BatchExecutionStatus.PARTIAL_FAILURE, BatchExecutionTriggerType.SCHEDULED, null)
        ));
        when(analysisReportBatchAssetResultRepository.findAllByBatchRunRunIdOrderByIdAsc("run-001")).thenReturn(List.of(
                assetResultEntity("BTCUSDT", BatchExecutionStatus.SUCCESS, null),
                assetResultEntity("ETHUSDT", BatchExecutionStatus.PARTIAL_FAILURE, null),
                assetResultEntity("XRPUSDT", BatchExecutionStatus.FAILED, "unexpected crash")
        ));

        AnalysisReportBatchRunDetailView detail = service.getRunDetail("run-001");

        assertThat(detail.runId()).isEqualTo("run-001");
        assertThat(detail.rerunnable()).isTrue();
        assertThat(detail.rerunnableSymbols()).containsExactly("ETHUSDT", "XRPUSDT");
        assertThat(detail.assetResults()).hasSize(3);
        assertThat(detail.assetResults().get(0).snapshotResults()).hasSize(1);
        assertThat(detail.assetResults().get(1).reportResults()).hasSize(1);
        assertThat(detail.assetResults().get(2).crashErrorMessage()).isEqualTo("unexpected crash");
    }

    private AnalysisReportBatchRunEntity runEntity(
            String runId,
            BatchExecutionStatus status,
            BatchExecutionTriggerType triggerType,
            String rerunSourceRunId
    ) {
        return AnalysisReportBatchRunEntity.builder()
                                           .runId(runId)
                                           .executionStatus(status)
                                           .triggerType(triggerType)
                                           .rerunSourceRunId(rerunSourceRunId)
                                           .engineVersion("report-assembler-v1")
                                           .startedAt(Instant.parse("2026-03-09T01:00:00Z"))
                                           .finishedAt(Instant.parse("2026-03-09T01:00:10Z"))
                                           .durationMillis(10000L)
                                           .assetSuccessCount(status == BatchExecutionStatus.SUCCESS ? 3 : 1)
                                           .assetFailureCount(status == BatchExecutionStatus.SUCCESS ? 0 : 2)
                                           .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                                           .build();
    }

    private AnalysisReportBatchAssetResultEntity assetResultEntity(
            String symbol,
            BatchExecutionStatus status,
            String crashErrorMessage
    ) throws Exception {
        return AnalysisReportBatchAssetResultEntity.builder()
                                                   .batchRun(runEntity("run-001", BatchExecutionStatus.PARTIAL_FAILURE, BatchExecutionTriggerType.SCHEDULED, null))
                                                   .symbol(symbol)
                                                   .executionStatus(status)
                                                   .startedAt(Instant.parse("2026-03-09T01:00:00Z"))
                                                   .finishedAt(Instant.parse("2026-03-09T01:00:05Z"))
                                                   .durationMillis(5000L)
                                                   .snapshotSuccessCount(status == BatchExecutionStatus.SUCCESS ? 1 : 0)
                                                   .snapshotFailureCount(status == BatchExecutionStatus.SUCCESS ? 0 : 1)
                                                   .reportSuccessCount(status == BatchExecutionStatus.SUCCESS ? 1 : 0)
                                                   .reportFailureCount(status == BatchExecutionStatus.SUCCESS ? 0 : 1)
                                                   .crashErrorMessage(crashErrorMessage)
                                                   .snapshotResultsPayload(objectMapper.writeValueAsString(
                                                           List.of(new AnalysisReportSnapshotStepResult(CandleInterval.ONE_HOUR, status == BatchExecutionStatus.SUCCESS, crashErrorMessage))
                                                   ))
                                                   .reportResultsPayload(objectMapper.writeValueAsString(
                                                           List.of(new AnalysisReportStepResult(AnalysisReportType.SHORT_TERM, status == BatchExecutionStatus.SUCCESS, crashErrorMessage))
                                                   ))
                                                   .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                                                   .build();
    }
}

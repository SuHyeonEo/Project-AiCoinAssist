package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchRerunServiceTest {

    @Mock
    private AnalysisReportBatchRunRepository analysisReportBatchRunRepository;

    @Mock
    private AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;

    @Mock
    private AnalysisReportBatchExecutionService analysisReportBatchExecutionService;

    @Test
    void rerunFailedAssetsUsesOriginalEngineVersionAndFailedAssetSubset() {
        AnalysisReportBatchRerunService service = new AnalysisReportBatchRerunService(
                analysisReportBatchRunRepository,
                analysisReportBatchAssetResultRepository,
                analysisReportBatchExecutionService
        );

        AnalysisReportBatchRunEntity sourceRun = AnalysisReportBatchRunEntity.builder()
                .runId("run-001")
                .executionStatus(BatchExecutionStatus.PARTIAL_FAILURE)
                .triggerType(BatchExecutionTriggerType.SCHEDULED)
                .rerunSourceRunId(null)
                .engineVersion("report-assembler-v1")
                .startedAt(Instant.parse("2026-03-09T01:00:00Z"))
                .finishedAt(Instant.parse("2026-03-09T01:00:10Z"))
                .durationMillis(10000L)
                .assetSuccessCount(1)
                .assetFailureCount(1)
                .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                .build();

        when(analysisReportBatchRunRepository.findByRunId("run-001")).thenReturn(Optional.of(sourceRun));
        when(analysisReportBatchAssetResultRepository.findAllByBatchRunRunIdAndExecutionStatusNotOrderByIdAsc(
                "run-001",
                BatchExecutionStatus.SUCCESS
        )).thenReturn(List.of(
                AnalysisReportBatchAssetResultEntity.builder()
                        .batchRun(sourceRun)
                        .symbol("ETHUSDT")
                        .executionStatus(BatchExecutionStatus.PARTIAL_FAILURE)
                        .startedAt(Instant.parse("2026-03-09T01:00:01Z"))
                        .finishedAt(Instant.parse("2026-03-09T01:00:05Z"))
                        .durationMillis(4000L)
                        .snapshotSuccessCount(1)
                        .snapshotFailureCount(1)
                        .reportSuccessCount(0)
                        .reportFailureCount(1)
                        .snapshotResultsPayload("[]")
                        .reportResultsPayload("[]")
                        .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                        .build()
        ));

        AnalysisReportBatchRunResult rerunResult = new AnalysisReportBatchRunResult(
                "run-002",
                BatchExecutionTriggerType.MANUAL_RERUN,
                "run-001",
                Instant.parse("2026-03-09T02:00:00Z"),
                Instant.parse("2026-03-09T02:00:05Z"),
                5000L,
                List.of()
        );
        when(analysisReportBatchExecutionService.execute(
                List.of(AssetType.ETH),
                "report-assembler-v1",
                BatchExecutionTriggerType.MANUAL_RERUN,
                "run-001"
        )).thenReturn(rerunResult);

        AnalysisReportBatchRunResult result = service.rerunFailedAssets("run-001");

        assertThat(result).isSameAs(rerunResult);
        verify(analysisReportBatchExecutionService).execute(
                List.of(AssetType.ETH),
                "report-assembler-v1",
                BatchExecutionTriggerType.MANUAL_RERUN,
                "run-001"
        );
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchAssetResultView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunDetailView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunSummaryView;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportSnapshotStepResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportStepResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchRunReadService {

    private static final TypeReference<List<AnalysisReportSnapshotStepResult>> SNAPSHOT_STEP_RESULTS =
            new TypeReference<>() {};
    private static final TypeReference<List<AnalysisReportStepResult>> REPORT_STEP_RESULTS =
            new TypeReference<>() {};

    private final AnalysisReportBatchRunRepository analysisReportBatchRunRepository;
    private final AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;
    private final ObjectMapper objectMapper;

    public List<AnalysisReportBatchRunSummaryView> listRecentRuns(int limit) {
        int safeLimit = Math.max(1, limit);
        return analysisReportBatchRunRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, safeLimit))
                                               .stream()
                                               .map(this::summaryView)
                                               .toList();
    }

    public AnalysisReportBatchRunDetailView getRunDetail(String runId) {
        AnalysisReportBatchRunEntity runEntity = analysisReportBatchRunRepository.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + runId));

        List<AnalysisReportBatchAssetResultView> assetResults = analysisReportBatchAssetResultRepository
                .findAllByBatchRunRunIdOrderByIdAsc(runId)
                .stream()
                .map(this::assetResultView)
                .toList();

        List<String> rerunnableSymbols = assetResults.stream()
                                                     .filter(result -> result.executionStatus() != BatchExecutionStatus.SUCCESS)
                                                     .map(AnalysisReportBatchAssetResultView::symbol)
                                                     .toList();

        return new AnalysisReportBatchRunDetailView(
                runEntity.getRunId(),
                runEntity.getExecutionStatus(),
                runEntity.getTriggerType(),
                runEntity.getRerunSourceRunId(),
                runEntity.getEngineVersion(),
                runEntity.getStartedAt(),
                runEntity.getFinishedAt(),
                runEntity.getDurationMillis(),
                runEntity.getAssetSuccessCount(),
                runEntity.getAssetFailureCount(),
                runEntity.getStoredTime(),
                !rerunnableSymbols.isEmpty(),
                rerunnableSymbols,
                assetResults
        );
    }

    private AnalysisReportBatchRunSummaryView summaryView(AnalysisReportBatchRunEntity entity) {
        return new AnalysisReportBatchRunSummaryView(
                entity.getRunId(),
                entity.getExecutionStatus(),
                entity.getTriggerType(),
                entity.getRerunSourceRunId(),
                entity.getEngineVersion(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMillis(),
                entity.getAssetSuccessCount(),
                entity.getAssetFailureCount(),
                entity.getAssetFailureCount() > 0
        );
    }

    private AnalysisReportBatchAssetResultView assetResultView(AnalysisReportBatchAssetResultEntity entity) {
        return new AnalysisReportBatchAssetResultView(
                entity.getSymbol(),
                entity.getExecutionStatus(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMillis(),
                entity.getSnapshotSuccessCount(),
                entity.getSnapshotFailureCount(),
                entity.getReportSuccessCount(),
                entity.getReportFailureCount(),
                entity.getCrashErrorMessage(),
                readSnapshotResults(entity.getSnapshotResultsPayload()),
                readReportResults(entity.getReportResultsPayload())
        );
    }

    private List<AnalysisReportSnapshotStepResult> readSnapshotResults(String payload) {
        return readJson(payload, SNAPSHOT_STEP_RESULTS, "snapshot step results");
    }

    private List<AnalysisReportStepResult> readReportResults(String payload) {
        return readJson(payload, REPORT_STEP_RESULTS, "report step results");
    }

    private <T> T readJson(String payload, TypeReference<T> typeReference, String fieldName) {
        try {
            return objectMapper.readValue(payload, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize " + fieldName + ".", exception);
        }
    }
}

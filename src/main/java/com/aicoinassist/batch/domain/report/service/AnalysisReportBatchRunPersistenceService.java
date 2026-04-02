package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchRunPersistenceService {

    private final AnalysisReportBatchRunRepository analysisReportBatchRunRepository;
    private final AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisReportBatchRunEntity save(
            AnalysisReportBatchRunResult runResult,
            String engineVersion,
            Instant storedTime
    ) {
        AnalysisReportBatchRunEntity runEntity = analysisReportBatchRunRepository.save(
                AnalysisReportBatchRunEntity.builder()
                                            .runId(runResult.runId())
                                            .executionStatus(runResult.status())
                                            .triggerType(runResult.triggerType())
                                            .rerunSourceRunId(runResult.rerunSourceRunId())
                                            .engineVersion(engineVersion)
                                            .startedAt(runResult.startedAt())
                                            .finishedAt(runResult.finishedAt())
                                            .durationMillis(runResult.durationMillis())
                                            .assetSuccessCount(runResult.assetSuccessCount())
                                            .assetFailureCount(runResult.assetFailureCount())
                                            .storedTime(storedTime)
                                            .build()
        );

        analysisReportBatchAssetResultRepository.saveAll(
                runResult.assetResults()
                         .stream()
                         .map(result -> assetResultEntity(runEntity, result, storedTime))
                         .toList()
        );
        return runEntity;
    }

    private AnalysisReportBatchAssetResultEntity assetResultEntity(
            AnalysisReportBatchRunEntity runEntity,
            AnalysisReportBatchResult result,
            Instant storedTime
    ) {
        return AnalysisReportBatchAssetResultEntity.builder()
                                                   .batchRun(runEntity)
                                                   .symbol(result.symbol())
                                                   .executionStatus(result.status())
                                                   .startedAt(result.startedAt())
                                                   .finishedAt(result.finishedAt())
                                                   .durationMillis(result.durationMillis())
                                                   .snapshotSuccessCount(result.snapshotSuccessCount())
                                                   .snapshotFailureCount(result.snapshotFailureCount())
                                                   .reportSuccessCount(result.reportSuccessCount())
                                                   .reportFailureCount(result.reportFailureCount())
                                                   .crashErrorMessage(result.crashErrorMessage())
                                                   .snapshotResultsPayload(writeJson(result.snapshotResults()))
                                                   .reportResultsPayload(writeJson(result.reportResults()))
                                                   .storedTime(storedTime)
                                                   .build();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize analysis report batch result payload.", exception);
        }
    }
}

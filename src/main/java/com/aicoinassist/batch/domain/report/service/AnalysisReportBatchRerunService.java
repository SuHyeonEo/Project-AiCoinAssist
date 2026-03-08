package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchRunResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchAssetResultRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportBatchRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportBatchRerunService {

    private final AnalysisReportBatchRunRepository analysisReportBatchRunRepository;
    private final AnalysisReportBatchAssetResultRepository analysisReportBatchAssetResultRepository;
    private final AnalysisReportBatchExecutionService analysisReportBatchExecutionService;

    public AnalysisReportBatchRunResult rerunFailedAssets(String sourceRunId) {
        AnalysisReportBatchRunEntity sourceRun = analysisReportBatchRunRepository.findByRunId(sourceRunId)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + sourceRunId));

        List<AssetType> failedAssetTypes = analysisReportBatchAssetResultRepository
                .findAllByBatchRunRunIdAndExecutionStatusNotOrderByIdAsc(sourceRunId, BatchExecutionStatus.SUCCESS)
                .stream()
                .map(assetResult -> AssetType.fromSymbol(assetResult.getSymbol()))
                .toList();

        if (failedAssetTypes.isEmpty()) {
            throw new IllegalStateException("No failed asset results found for runId: " + sourceRunId);
        }

        return analysisReportBatchExecutionService.execute(
                failedAssetTypes,
                sourceRun.getEngineVersion(),
                BatchExecutionTriggerType.MANUAL_RERUN,
                sourceRunId
        );
    }
}

package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchAssetResultEntity;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisReportBatchAssetResultRepository extends JpaRepository<AnalysisReportBatchAssetResultEntity, Long> {

    List<AnalysisReportBatchAssetResultEntity> findAllByBatchRunRunIdAndExecutionStatusNotOrderByIdAsc(
            String runId,
            BatchExecutionStatus executionStatus
    );

    List<AnalysisReportBatchAssetResultEntity> findAllByBatchRunRunIdOrderByIdAsc(String runId);
}

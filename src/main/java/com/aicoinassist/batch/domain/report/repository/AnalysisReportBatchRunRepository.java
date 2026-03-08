package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisReportBatchRunRepository extends JpaRepository<AnalysisReportBatchRunEntity, Long> {

    Optional<AnalysisReportBatchRunEntity> findByRunId(String runId);
}

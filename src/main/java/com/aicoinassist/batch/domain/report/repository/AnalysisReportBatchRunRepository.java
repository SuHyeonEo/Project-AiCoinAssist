package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportBatchRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportBatchRunRepository extends JpaRepository<AnalysisReportBatchRunEntity, Long> {

    Optional<AnalysisReportBatchRunEntity> findByRunId(String runId);

    List<AnalysisReportBatchRunEntity> findAllByOrderByStartedAtDesc(Pageable pageable);
}

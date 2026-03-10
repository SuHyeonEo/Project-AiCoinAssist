package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReportEntity, Long> {

    Optional<AnalysisReportEntity> findTopBySymbolAndReportTypeAndAnalysisBasisTimeAndSourceDataVersionAndAnalysisEngineVersionOrderByIdDesc(
            String symbol,
            AnalysisReportType reportType,
            Instant analysisBasisTime,
            String sourceDataVersion,
            String analysisEngineVersion
    );

    Optional<AnalysisReportEntity> findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
            String symbol,
            AnalysisReportType reportType,
            Instant analysisBasisTime
    );

    Optional<AnalysisReportEntity> findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
            String symbol,
            AnalysisReportType reportType
    );
}

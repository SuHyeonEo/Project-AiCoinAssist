package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReportEntity, Long> {

    @Query("select distinct ar.symbol from AnalysisReportEntity ar order by ar.symbol")
    List<String> findDistinctSymbols();

    Optional<AnalysisReportEntity> findTopBySymbolOrderByAnalysisBasisTimeDescIdDesc(String symbol);

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

    Page<AnalysisReportEntity> findBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
            String symbol,
            AnalysisReportType reportType,
            Pageable pageable
    );
}

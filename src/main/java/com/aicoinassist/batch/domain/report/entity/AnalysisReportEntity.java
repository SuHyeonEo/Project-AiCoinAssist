package com.aicoinassist.batch.domain.report.entity;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "analysis_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_report_symbol_type_basis_version_engine",
                        columnNames = {
                                "symbol",
                                "report_type",
                                "analysis_basis_time",
                                "source_data_version",
                                "analysis_engine_version"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_analysis_report_symbol_type_basis_time",
                        columnList = "symbol, report_type, analysis_basis_time"
                ),
                @Index(
                        name = "idx_analysis_report_raw_reference_time",
                        columnList = "raw_reference_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisReportType reportType;

    @Column(nullable = false)
    private Instant analysisBasisTime;

    @Column(nullable = false)
    private Instant rawReferenceTime;

    @Column(nullable = false, length = 200)
    private String sourceDataVersion;

    @Column(nullable = false, length = 100)
    private String analysisEngineVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportPayload;

    @Column(nullable = false)
    private Instant storedTime;

    @Builder
    public AnalysisReportEntity(
            String symbol,
            AnalysisReportType reportType,
            Instant analysisBasisTime,
            Instant rawReferenceTime,
            String sourceDataVersion,
            String analysisEngineVersion,
            String reportPayload,
            Instant storedTime
    ) {
        this.symbol = symbol;
        this.reportType = reportType;
        this.analysisBasisTime = analysisBasisTime;
        this.rawReferenceTime = rawReferenceTime;
        this.sourceDataVersion = sourceDataVersion;
        this.analysisEngineVersion = analysisEngineVersion;
        this.reportPayload = reportPayload;
        this.storedTime = storedTime;
    }

    public void refresh(
            Instant rawReferenceTime,
            String reportPayload,
            Instant storedTime
    ) {
        this.rawReferenceTime = rawReferenceTime;
        this.reportPayload = reportPayload;
        this.storedTime = storedTime;
    }
}

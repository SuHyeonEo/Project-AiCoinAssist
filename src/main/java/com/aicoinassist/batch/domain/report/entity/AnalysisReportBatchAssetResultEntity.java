package com.aicoinassist.batch.domain.report.entity;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "analysis_report_batch_asset_result",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_report_batch_asset_result_run_symbol",
                        columnNames = {"batch_run_id", "symbol"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_analysis_report_batch_asset_result_run_id",
                        columnList = "batch_run_id"
                ),
                @Index(
                        name = "idx_analysis_report_batch_asset_result_symbol_status",
                        columnList = "symbol, execution_status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReportBatchAssetResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_run_id", nullable = false)
    private AnalysisReportBatchRunEntity batchRun;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchExecutionStatus executionStatus;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant finishedAt;

    @Column(nullable = false)
    private long durationMillis;

    @Column(nullable = false)
    private int snapshotSuccessCount;

    @Column(nullable = false)
    private int snapshotFailureCount;

    @Column(nullable = false)
    private int reportSuccessCount;

    @Column(nullable = false)
    private int reportFailureCount;

    @Column(columnDefinition = "TEXT")
    private String crashErrorMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotResultsPayload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportResultsPayload;

    @Column(nullable = false)
    private Instant storedTime;

    @Builder
    public AnalysisReportBatchAssetResultEntity(
            AnalysisReportBatchRunEntity batchRun,
            String symbol,
            BatchExecutionStatus executionStatus,
            Instant startedAt,
            Instant finishedAt,
            long durationMillis,
            int snapshotSuccessCount,
            int snapshotFailureCount,
            int reportSuccessCount,
            int reportFailureCount,
            String crashErrorMessage,
            String snapshotResultsPayload,
            String reportResultsPayload,
            Instant storedTime
    ) {
        this.batchRun = batchRun;
        this.symbol = symbol;
        this.executionStatus = executionStatus;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMillis = durationMillis;
        this.snapshotSuccessCount = snapshotSuccessCount;
        this.snapshotFailureCount = snapshotFailureCount;
        this.reportSuccessCount = reportSuccessCount;
        this.reportFailureCount = reportFailureCount;
        this.crashErrorMessage = crashErrorMessage;
        this.snapshotResultsPayload = snapshotResultsPayload;
        this.reportResultsPayload = reportResultsPayload;
        this.storedTime = storedTime;
    }
}

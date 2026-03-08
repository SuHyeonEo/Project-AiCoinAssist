package com.aicoinassist.batch.domain.report.entity;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
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
        name = "analysis_report_batch_run",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_report_batch_run_run_id",
                        columnNames = "run_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_analysis_report_batch_run_started_at",
                        columnList = "started_at"
                ),
                @Index(
                        name = "idx_analysis_report_batch_run_status",
                        columnList = "execution_status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReportBatchRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String runId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchExecutionStatus executionStatus;

    @Column(nullable = false, length = 100)
    private String engineVersion;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant finishedAt;

    @Column(nullable = false)
    private long durationMillis;

    @Column(nullable = false)
    private int assetSuccessCount;

    @Column(nullable = false)
    private int assetFailureCount;

    @Column(nullable = false)
    private Instant storedTime;

    @Builder
    public AnalysisReportBatchRunEntity(
            String runId,
            BatchExecutionStatus executionStatus,
            String engineVersion,
            Instant startedAt,
            Instant finishedAt,
            long durationMillis,
            int assetSuccessCount,
            int assetFailureCount,
            Instant storedTime
    ) {
        this.runId = runId;
        this.executionStatus = executionStatus;
        this.engineVersion = engineVersion;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMillis = durationMillis;
        this.assetSuccessCount = assetSuccessCount;
        this.assetFailureCount = assetFailureCount;
        this.storedTime = storedTime;
    }
}

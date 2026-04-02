package com.aicoinassist.batch.domain.report.entity;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
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
        name = "analysis_report_shared_context",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_report_shared_context_identity",
                        columnNames = {
                                "context_version",
                                "llm_provider",
                                "llm_model",
                                "prompt_template_version",
                                "input_schema_version",
                                "output_schema_version",
                                "input_payload_hash"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_analysis_report_shared_context_basis",
                        columnList = "analysis_basis_time"
                ),
                @Index(
                        name = "idx_analysis_report_shared_context_status_stored_time",
                        columnList = "generation_status, stored_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReportSharedContextEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisReportType reportType;

    @Column(nullable = false)
    private Instant analysisBasisTime;

    @Column(nullable = false)
    private Instant rawReferenceTime;

    @Column(nullable = false, length = 64)
    private String contextVersion;

    @Column(nullable = false, length = 100)
    private String analysisEngineVersion;

    @Column(nullable = false, length = 30)
    private String llmProvider;

    @Column(nullable = false, length = 100)
    private String llmModel;

    @Column(nullable = false, length = 50)
    private String promptTemplateVersion;

    @Column(nullable = false, length = 50)
    private String inputSchemaVersion;

    @Column(nullable = false, length = 50)
    private String outputSchemaVersion;

    @Column(nullable = false, length = 64)
    private String inputPayloadHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputPayloadJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptSystemText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptUserText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputLengthPolicyJson;

    @Column(columnDefinition = "TEXT")
    private String rawOutputText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputJson;

    @Column(nullable = false)
    private boolean fallbackUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisLlmNarrativeGenerationStatus generationStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AnalysisLlmNarrativeFailureType failureType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String validationIssuesJson;

    @Column(length = 120)
    private String providerRequestId;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = false)
    private Instant completedAt;

    @Column(nullable = false)
    private Instant storedAt;

    @Builder
    public AnalysisReportSharedContextEntity(
            AnalysisReportType reportType,
            Instant analysisBasisTime,
            Instant rawReferenceTime,
            String contextVersion,
            String analysisEngineVersion,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            String inputPayloadHash,
            String inputPayloadJson,
            String promptSystemText,
            String promptUserText,
            String outputLengthPolicyJson,
            String rawOutputText,
            String outputJson,
            boolean fallbackUsed,
            AnalysisLlmNarrativeGenerationStatus generationStatus,
            AnalysisLlmNarrativeFailureType failureType,
            String validationIssuesJson,
            String providerRequestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        this.reportType = reportType;
        this.analysisBasisTime = analysisBasisTime;
        this.rawReferenceTime = rawReferenceTime;
        this.contextVersion = contextVersion;
        this.analysisEngineVersion = analysisEngineVersion;
        this.llmProvider = llmProvider;
        this.llmModel = llmModel;
        this.promptTemplateVersion = promptTemplateVersion;
        this.inputSchemaVersion = inputSchemaVersion;
        this.outputSchemaVersion = outputSchemaVersion;
        this.inputPayloadHash = inputPayloadHash;
        this.inputPayloadJson = inputPayloadJson;
        this.promptSystemText = promptSystemText;
        this.promptUserText = promptUserText;
        this.outputLengthPolicyJson = outputLengthPolicyJson;
        this.rawOutputText = rawOutputText;
        this.outputJson = outputJson;
        this.fallbackUsed = fallbackUsed;
        this.generationStatus = generationStatus;
        this.failureType = failureType;
        this.validationIssuesJson = validationIssuesJson;
        this.providerRequestId = providerRequestId;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.storedAt = storedAt;
    }

    public void refresh(
            String inputPayloadJson,
            String promptSystemText,
            String promptUserText,
            String outputLengthPolicyJson,
            String rawOutputText,
            String outputJson,
            boolean fallbackUsed,
            AnalysisLlmNarrativeGenerationStatus generationStatus,
            AnalysisLlmNarrativeFailureType failureType,
            String validationIssuesJson,
            String providerRequestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        this.inputPayloadJson = inputPayloadJson;
        this.promptSystemText = promptSystemText;
        this.promptUserText = promptUserText;
        this.outputLengthPolicyJson = outputLengthPolicyJson;
        this.rawOutputText = rawOutputText;
        this.outputJson = outputJson;
        this.fallbackUsed = fallbackUsed;
        this.generationStatus = generationStatus;
        this.failureType = failureType;
        this.validationIssuesJson = validationIssuesJson;
        this.providerRequestId = providerRequestId;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.storedAt = storedAt;
    }
}

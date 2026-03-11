package com.aicoinassist.batch.domain.news.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "reference_news_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reference_news_snapshot_scope_date",
                        columnNames = {"scope", "snapshot_date"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_reference_news_snapshot_scope_date",
                        columnList = "scope, snapshot_date"
                ),
                @Index(
                        name = "idx_reference_news_snapshot_stored_time",
                        columnList = "stored_time"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReferenceNewsSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String scope;

    @Column(nullable = false)
    private LocalDate snapshotDate;

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

    @Column(nullable = false)
    private Integer articleCount;

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
    private String payloadJson;

    @Column(length = 120)
    private String providerRequestId;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = false)
    private Instant completedAt;

    @Column(name = "stored_time", nullable = false)
    private Instant storedAt;

    @Builder
    public ReferenceNewsSnapshotEntity(
            String scope,
            LocalDate snapshotDate,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            Integer articleCount,
            String inputPayloadJson,
            String promptSystemText,
            String promptUserText,
            String outputLengthPolicyJson,
            String rawOutputText,
            String payloadJson,
            String providerRequestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        this.scope = scope;
        this.snapshotDate = snapshotDate;
        this.llmProvider = llmProvider;
        this.llmModel = llmModel;
        this.promptTemplateVersion = promptTemplateVersion;
        this.inputSchemaVersion = inputSchemaVersion;
        this.outputSchemaVersion = outputSchemaVersion;
        this.articleCount = articleCount;
        this.inputPayloadJson = inputPayloadJson;
        this.promptSystemText = promptSystemText;
        this.promptUserText = promptUserText;
        this.outputLengthPolicyJson = outputLengthPolicyJson;
        this.rawOutputText = rawOutputText;
        this.payloadJson = payloadJson;
        this.providerRequestId = providerRequestId;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.storedAt = storedAt;
    }

    public void refresh(
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            Integer articleCount,
            String inputPayloadJson,
            String promptSystemText,
            String promptUserText,
            String outputLengthPolicyJson,
            String rawOutputText,
            String payloadJson,
            String providerRequestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens,
            Instant requestedAt,
            Instant completedAt,
            Instant storedAt
    ) {
        this.llmProvider = llmProvider;
        this.llmModel = llmModel;
        this.promptTemplateVersion = promptTemplateVersion;
        this.inputSchemaVersion = inputSchemaVersion;
        this.outputSchemaVersion = outputSchemaVersion;
        this.articleCount = articleCount;
        this.inputPayloadJson = inputPayloadJson;
        this.promptSystemText = promptSystemText;
        this.promptUserText = promptUserText;
        this.outputLengthPolicyJson = outputLengthPolicyJson;
        this.rawOutputText = rawOutputText;
        this.payloadJson = payloadJson;
        this.providerRequestId = providerRequestId;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.storedAt = storedAt;
    }
}

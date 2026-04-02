package com.aicoinassist.batch.domain.macro.entity;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "macro_context_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_macro_context_snapshot_snapshot_time",
                        columnNames = {"snapshot_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_macro_context_snapshot_snapshot_time",
                        columnList = "snapshot_time"
                ),
                @Index(
                        name = "idx_macro_context_snapshot_source_data_version",
                        columnList = "source_data_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MacroContextSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(nullable = false)
    private LocalDate dxyObservationDate;

    @Column(nullable = false)
    private LocalDate us10yYieldObservationDate;

    @Column(nullable = false)
    private LocalDate usdKrwObservationDate;

    @Column(nullable = false, length = 200)
    private String sourceDataVersion;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal dxyProxyValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal us10yYieldValue;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal usdKrwValue;

    @Builder
    public MacroContextSnapshotEntity(
            Instant snapshotTime,
            LocalDate dxyObservationDate,
            LocalDate us10yYieldObservationDate,
            LocalDate usdKrwObservationDate,
            String sourceDataVersion,
            BigDecimal dxyProxyValue,
            BigDecimal us10yYieldValue,
            BigDecimal usdKrwValue
    ) {
        this.snapshotTime = snapshotTime;
        this.dxyObservationDate = dxyObservationDate;
        this.us10yYieldObservationDate = us10yYieldObservationDate;
        this.usdKrwObservationDate = usdKrwObservationDate;
        this.sourceDataVersion = sourceDataVersion;
        this.dxyProxyValue = dxyProxyValue;
        this.us10yYieldValue = us10yYieldValue;
        this.usdKrwValue = usdKrwValue;
    }

    public void refreshFromSnapshot(
            LocalDate dxyObservationDate,
            LocalDate us10yYieldObservationDate,
            LocalDate usdKrwObservationDate,
            String sourceDataVersion,
            BigDecimal dxyProxyValue,
            BigDecimal us10yYieldValue,
            BigDecimal usdKrwValue
    ) {
        this.dxyObservationDate = dxyObservationDate;
        this.us10yYieldObservationDate = us10yYieldObservationDate;
        this.usdKrwObservationDate = usdKrwObservationDate;
        this.sourceDataVersion = sourceDataVersion;
        this.dxyProxyValue = dxyProxyValue;
        this.us10yYieldValue = us10yYieldValue;
        this.usdKrwValue = usdKrwValue;
    }
}

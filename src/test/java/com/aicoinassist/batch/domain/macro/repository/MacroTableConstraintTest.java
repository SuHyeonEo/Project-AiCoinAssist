package com.aicoinassist.batch.domain.macro.repository;

import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MacroTableConstraintTest {

    @Autowired
    private MacroSnapshotRawRepository macroSnapshotRawRepository;

    @Autowired
    private MacroContextSnapshotRepository macroContextSnapshotRepository;

    @Test
    void macroSnapshotRawRejectsDuplicateSourceMetricAndObservationDate() {
        LocalDate observationDate = LocalDate.parse("2026-03-10");

        macroSnapshotRawRepository.saveAndFlush(rawEntity(observationDate, MacroMetricType.DXY_PROXY, "119.84210000"));

        assertThatThrownBy(() -> macroSnapshotRawRepository.saveAndFlush(
                rawEntity(observationDate, MacroMetricType.DXY_PROXY, "120.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void macroContextSnapshotRejectsDuplicateSnapshotTime() {
        Instant snapshotTime = Instant.parse("2026-03-10T00:00:00Z");

        macroContextSnapshotRepository.saveAndFlush(snapshotEntity(snapshotTime, "119.84210000"));

        assertThatThrownBy(() -> macroContextSnapshotRepository.saveAndFlush(
                snapshotEntity(snapshotTime, "120.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private MacroSnapshotRawEntity rawEntity(LocalDate observationDate, MacroMetricType metricType, String metricValue) {
        return MacroSnapshotRawEntity.builder()
                                     .source("FRED")
                                     .metricType(metricType)
                                     .seriesId("series")
                                     .units("lin")
                                     .observationDate(observationDate)
                                     .collectedTime(observationDate.atStartOfDay().plusMinutes(1).toInstant(java.time.ZoneOffset.UTC))
                                     .validationStatus(RawDataValidationStatus.VALID)
                                     .metricValue(new BigDecimal(metricValue))
                                     .rawPayload("{\"value\":\"" + metricValue + "\"}")
                                     .build();
    }

    private MacroContextSnapshotEntity snapshotEntity(Instant snapshotTime, String dxyValue) {
        return MacroContextSnapshotEntity.builder()
                                         .snapshotTime(snapshotTime)
                                         .dxyObservationDate(LocalDate.parse("2026-03-10"))
                                         .us10yYieldObservationDate(LocalDate.parse("2026-03-10"))
                                         .usdKrwObservationDate(LocalDate.parse("2026-03-09"))
                                         .sourceDataVersion("dxyProxyDate=2026-03-10;us10yYieldDate=2026-03-10;usdKrwDate=2026-03-09")
                                         .dxyProxyValue(new BigDecimal(dxyValue))
                                         .us10yYieldValue(new BigDecimal("4.12000000"))
                                         .usdKrwValue(new BigDecimal("1453.22000000"))
                                         .build();
    }
}

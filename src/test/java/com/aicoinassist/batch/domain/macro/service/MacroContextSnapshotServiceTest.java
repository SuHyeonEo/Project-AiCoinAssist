package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.macro.repository.MacroSnapshotRawRepository;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MacroContextSnapshotServiceTest {

    @Mock
    private MacroSnapshotRawRepository macroSnapshotRawRepository;

    @Test
    void createBuildsProcessedSnapshotFromLatestValidMacroRaw() {
        MacroContextSnapshotService service = new MacroContextSnapshotService(macroSnapshotRawRepository);

        when(macroSnapshotRawRepository.findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(MacroMetricType.DXY_PROXY))
                .thenReturn(Optional.of(rawEntity(MacroMetricType.DXY_PROXY, "119.84210000", "2026-03-10")));
        when(macroSnapshotRawRepository.findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(MacroMetricType.US10Y_YIELD))
                .thenReturn(Optional.of(rawEntity(MacroMetricType.US10Y_YIELD, "4.12000000", "2026-03-10")));
        when(macroSnapshotRawRepository.findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(MacroMetricType.USD_KRW))
                .thenReturn(Optional.of(rawEntity(MacroMetricType.USD_KRW, "1453.22000000", "2026-03-09")));

        MacroContextSnapshot snapshot = service.create();

        assertThat(snapshot.snapshotTime()).isEqualTo(Instant.parse("2026-03-10T00:00:00Z"));
        assertThat(snapshot.dxyProxyValue()).isEqualByComparingTo("119.84210000");
        assertThat(snapshot.us10yYieldValue()).isEqualByComparingTo("4.12000000");
        assertThat(snapshot.usdKrwValue()).isEqualByComparingTo("1453.22000000");
        assertThat(snapshot.sourceDataVersion()).contains("dxyProxyDate=2026-03-10");
        assertThat(snapshot.sourceDataVersion()).contains("usdKrwDate=2026-03-09");
    }

    @Test
    void createRejectsInvalidMacroRawSnapshot() {
        MacroContextSnapshotService service = new MacroContextSnapshotService(macroSnapshotRawRepository);

        when(macroSnapshotRawRepository.findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(MacroMetricType.DXY_PROXY))
                .thenReturn(Optional.of(rawEntity(MacroMetricType.DXY_PROXY, "119.84210000", "2026-03-10")));
        when(macroSnapshotRawRepository.findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(MacroMetricType.US10Y_YIELD))
                .thenReturn(Optional.of(rawEntity(MacroMetricType.US10Y_YIELD, null, "2026-03-10")));

        assertThatThrownBy(service::create)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("US10Y_YIELD");
    }

    private MacroSnapshotRawEntity rawEntity(MacroMetricType metricType, String metricValue, String observationDate) {
        return MacroSnapshotRawEntity.builder()
                                     .source("FRED")
                                     .metricType(metricType)
                                     .seriesId("series")
                                     .units("lin")
                                     .observationDate(LocalDate.parse(observationDate))
                                     .collectedTime(Instant.parse(observationDate + "T00:01:00Z"))
                                     .validationStatus(metricValue == null ? RawDataValidationStatus.INVALID : RawDataValidationStatus.VALID)
                                     .validationDetails(metricValue == null ? "FRED observation response does not contain a usable numeric observation." : null)
                                     .metricValue(metricValue == null ? null : new BigDecimal(metricValue))
                                     .rawPayload("{\"series\":\"" + metricType + "\"}")
                                     .build();
    }
}

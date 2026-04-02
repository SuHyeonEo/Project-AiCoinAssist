package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.FredMacroRawSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.macro.repository.MacroSnapshotRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.fred.FredMacroClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MacroRawIngestionServiceTest {

    @Mock
    private FredMacroClient fredMacroClient;

    @Mock
    private MacroSnapshotRawRepository macroSnapshotRawRepository;

    @Test
    void ingestDxyProxyRefreshesExistingRawWhenKeyMatches() {
        MacroRawIngestionService service = new MacroRawIngestionService(
                fredMacroClient,
                macroSnapshotRawRepository
        );

        FredMacroRawSnapshot snapshot = rawSnapshot(MacroMetricType.DXY_PROXY, "119.84210000");
        MacroSnapshotRawEntity existingEntity = MacroSnapshotRawEntity.builder()
                                                                     .source("FRED")
                                                                     .metricType(MacroMetricType.DXY_PROXY)
                                                                     .seriesId("DTWEXBGS")
                                                                     .units("lin")
                                                                     .observationDate(snapshot.observationDate())
                                                                     .collectedTime(Instant.parse("2026-03-10T00:01:00Z"))
                                                                     .validationStatus(snapshot.validation().status())
                                                                     .validationDetails("old")
                                                                     .metricValue(new BigDecimal("118.00000000"))
                                                                     .rawPayload("{\"old\":true}")
                                                                     .build();

        when(fredMacroClient.fetchLatestObservation(MacroMetricType.DXY_PROXY)).thenReturn(snapshot);
        when(macroSnapshotRawRepository.findTopBySourceAndMetricTypeAndObservationDateOrderByCollectedTimeDescIdDesc(
                "FRED",
                MacroMetricType.DXY_PROXY,
                snapshot.observationDate()
        )).thenReturn(Optional.of(existingEntity));

        MacroSnapshotRawEntity result = service.ingestDxyProxy();

        verify(macroSnapshotRawRepository, never()).save(any(MacroSnapshotRawEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getMetricValue()).isEqualByComparingTo("119.84210000");
        assertThat(existingEntity.getSeriesId()).isEqualTo("DTWEXBGS");
    }

    @Test
    void ingestUsdKrwPersistsNewRawWhenKeyDoesNotExist() {
        MacroRawIngestionService service = new MacroRawIngestionService(
                fredMacroClient,
                macroSnapshotRawRepository
        );

        FredMacroRawSnapshot snapshot = rawSnapshot(MacroMetricType.USD_KRW, "1453.22000000");

        when(fredMacroClient.fetchLatestObservation(MacroMetricType.USD_KRW)).thenReturn(snapshot);
        when(macroSnapshotRawRepository.findTopBySourceAndMetricTypeAndObservationDateOrderByCollectedTimeDescIdDesc(
                "FRED",
                MacroMetricType.USD_KRW,
                snapshot.observationDate()
        )).thenReturn(Optional.empty());
        when(macroSnapshotRawRepository.save(any(MacroSnapshotRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MacroSnapshotRawEntity result = service.ingestUsdKrw();

        assertThat(result.getSource()).isEqualTo("FRED");
        assertThat(result.getMetricType()).isEqualTo(MacroMetricType.USD_KRW);
        assertThat(result.getValidationStatus()).isEqualTo(snapshot.validation().status());
        assertThat(result.getMetricValue()).isEqualByComparingTo("1453.22000000");
    }

    private FredMacroRawSnapshot rawSnapshot(MacroMetricType metricType, String metricValue) {
        return new FredMacroRawSnapshot(
                metricType,
                switch (metricType) {
                    case DXY_PROXY -> "DTWEXBGS";
                    case US10Y_YIELD -> "DGS10";
                    case USD_KRW -> "DEXKOUS";
                },
                "lin",
                LocalDate.parse("2026-03-10"),
                RawDataValidationResult.valid(),
                new BigDecimal(metricValue),
                "{\"observations\":[{\"date\":\"2026-03-10\",\"value\":\"" + metricValue + "\"}]}"
        );
    }
}

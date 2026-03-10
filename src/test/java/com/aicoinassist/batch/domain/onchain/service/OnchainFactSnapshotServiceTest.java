package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.onchain.dto.OnchainFactSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.domain.onchain.repository.OnchainSnapshotRawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnchainFactSnapshotServiceTest {

    @Mock
    private OnchainSnapshotRawRepository onchainSnapshotRawRepository;

    @Test
    void createBuildsProcessedSnapshotFromLatestValidRaw() {
        OnchainFactSnapshotService service = new OnchainFactSnapshotService(onchainSnapshotRawRepository);

        when(onchainSnapshotRawRepository.findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc("btc", OnchainMetricType.ACTIVE_ADDRESS_COUNT))
                .thenReturn(Optional.of(rawEntity("btc", OnchainMetricType.ACTIVE_ADDRESS_COUNT, "815234.00000000", RawDataValidationStatus.VALID)));
        when(onchainSnapshotRawRepository.findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc("btc", OnchainMetricType.TRANSACTION_COUNT))
                .thenReturn(Optional.of(rawEntity("btc", OnchainMetricType.TRANSACTION_COUNT, "412345.00000000", RawDataValidationStatus.VALID)));
        when(onchainSnapshotRawRepository.findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc("btc", OnchainMetricType.MARKET_CAP_USD))
                .thenReturn(Optional.of(rawEntity("btc", OnchainMetricType.MARKET_CAP_USD, "1712345678900.00000000", RawDataValidationStatus.VALID)));

        OnchainFactSnapshot snapshot = service.create("BTCUSDT");

        assertThat(snapshot.symbol()).isEqualTo("BTCUSDT");
        assertThat(snapshot.assetCode()).isEqualTo("btc");
        assertThat(snapshot.activeAddressCount()).isEqualByComparingTo("815234.00000000");
        assertThat(snapshot.transactionCount()).isEqualByComparingTo("412345.00000000");
        assertThat(snapshot.marketCapUsd()).isEqualByComparingTo("1712345678900.00000000");
        assertThat(snapshot.sourceDataVersion()).contains("activeAddressSourceEventTime=");
    }

    @Test
    void createRejectsInvalidRawSnapshot() {
        OnchainFactSnapshotService service = new OnchainFactSnapshotService(onchainSnapshotRawRepository);

        when(onchainSnapshotRawRepository.findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc("eth", OnchainMetricType.ACTIVE_ADDRESS_COUNT))
                .thenReturn(Optional.of(rawEntity("eth", OnchainMetricType.ACTIVE_ADDRESS_COUNT, null, RawDataValidationStatus.INVALID)));

        assertThatThrownBy(() -> service.create("ETHUSDT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVE_ADDRESS_COUNT");
    }

    private OnchainSnapshotRawEntity rawEntity(
            String assetCode,
            OnchainMetricType metricType,
            String value,
            RawDataValidationStatus validationStatus
    ) {
        return OnchainSnapshotRawEntity.builder()
                                       .source("COIN_METRICS")
                                       .assetCode(assetCode)
                                       .metricType(metricType)
                                       .sourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                       .collectedTime(Instant.parse("2026-03-09T00:01:00Z"))
                                       .validationStatus(validationStatus)
                                       .validationDetails(validationStatus == RawDataValidationStatus.INVALID ? "metric invalid" : null)
                                       .metricValue(value == null ? null : new BigDecimal(value))
                                       .rawPayload("{\"metric\":\"" + metricType + "\"}")
                                       .build();
    }
}

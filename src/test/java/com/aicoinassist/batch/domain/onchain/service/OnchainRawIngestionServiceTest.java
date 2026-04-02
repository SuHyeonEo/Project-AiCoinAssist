package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.onchain.dto.CoinMetricsOnchainRawSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.domain.onchain.repository.OnchainSnapshotRawRepository;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.CoinMetricsOnchainClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnchainRawIngestionServiceTest {

    @Mock
    private CoinMetricsOnchainClient coinMetricsOnchainClient;

    @Mock
    private OnchainSnapshotRawRepository onchainSnapshotRawRepository;

    @Test
    void ingestMetricRefreshesExistingRawWhenKeyMatches() {
        OnchainRawIngestionService service = new OnchainRawIngestionService(
                coinMetricsOnchainClient,
                onchainSnapshotRawRepository
        );

        CoinMetricsOnchainRawSnapshot snapshot = rawSnapshot(OnchainMetricType.ACTIVE_ADDRESS_COUNT, "815234.00000000");
        OnchainSnapshotRawEntity existingEntity = OnchainSnapshotRawEntity.builder()
                                                                          .source("COIN_METRICS")
                                                                          .assetCode("btc")
                                                                          .metricType(OnchainMetricType.ACTIVE_ADDRESS_COUNT)
                                                                          .sourceEventTime(snapshot.sourceEventTime())
                                                                          .collectedTime(snapshot.sourceEventTime().plusSeconds(10))
                                                                          .validationStatus(snapshot.validation().status())
                                                                          .validationDetails("old")
                                                                          .metricValue(new BigDecimal("800000.00000000"))
                                                                          .rawPayload("{\"old\":true}")
                                                                          .build();

        when(coinMetricsOnchainClient.fetchLatestMetric("btc", OnchainMetricType.ACTIVE_ADDRESS_COUNT)).thenReturn(snapshot);
        when(onchainSnapshotRawRepository.findTopBySourceAndAssetCodeAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "COIN_METRICS",
                "btc",
                OnchainMetricType.ACTIVE_ADDRESS_COUNT,
                snapshot.sourceEventTime()
        )).thenReturn(Optional.of(existingEntity));

        OnchainSnapshotRawEntity result = service.ingestMetric(AssetType.BTC, OnchainMetricType.ACTIVE_ADDRESS_COUNT);

        verify(onchainSnapshotRawRepository, never()).save(any(OnchainSnapshotRawEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getMetricValue()).isEqualByComparingTo("815234.00000000");
    }

    @Test
    void ingestMetricPersistsNewRawWhenKeyDoesNotExist() {
        OnchainRawIngestionService service = new OnchainRawIngestionService(
                coinMetricsOnchainClient,
                onchainSnapshotRawRepository
        );

        CoinMetricsOnchainRawSnapshot snapshot = rawSnapshot(OnchainMetricType.TRANSACTION_COUNT, "412345.00000000");

        when(coinMetricsOnchainClient.fetchLatestMetric("eth", OnchainMetricType.TRANSACTION_COUNT)).thenReturn(snapshot);
        when(onchainSnapshotRawRepository.findTopBySourceAndAssetCodeAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "COIN_METRICS",
                "eth",
                OnchainMetricType.TRANSACTION_COUNT,
                snapshot.sourceEventTime()
        )).thenReturn(Optional.empty());
        when(onchainSnapshotRawRepository.save(any(OnchainSnapshotRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OnchainSnapshotRawEntity result = service.ingestMetric(AssetType.ETH, OnchainMetricType.TRANSACTION_COUNT);

        assertThat(result.getAssetCode()).isEqualTo("eth");
        assertThat(result.getMetricType()).isEqualTo(OnchainMetricType.TRANSACTION_COUNT);
        assertThat(result.getMetricValue()).isEqualByComparingTo("412345.00000000");
    }

    private CoinMetricsOnchainRawSnapshot rawSnapshot(OnchainMetricType metricType, String value) {
        return new CoinMetricsOnchainRawSnapshot(
                "btc",
                metricType,
                Instant.parse("2026-03-09T00:00:00Z"),
                RawDataValidationResult.valid(),
                new BigDecimal(value),
                "{\"data\":[{\"asset\":\"btc\",\"time\":\"2026-03-09T00:00:00.000000000Z\"}]}"
        );
    }
}

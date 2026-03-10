package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.onchain.dto.CoinMetricsOnchainRawSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.domain.onchain.repository.OnchainSnapshotRawRepository;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.CoinMetricsOnchainClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OnchainRawIngestionService {

    private static final String COIN_METRICS_SOURCE = "COIN_METRICS";

    private final CoinMetricsOnchainClient coinMetricsOnchainClient;
    private final OnchainSnapshotRawRepository onchainSnapshotRawRepository;

    @Transactional
    public void ingestAll(AssetType assetType) {
        ingestMetric(assetType, OnchainMetricType.ACTIVE_ADDRESS_COUNT);
        ingestMetric(assetType, OnchainMetricType.TRANSACTION_COUNT);
        ingestMetric(assetType, OnchainMetricType.MARKET_CAP_USD);
    }

    @Transactional
    public OnchainSnapshotRawEntity ingestMetric(AssetType assetType, OnchainMetricType metricType) {
        Instant collectedTime = Instant.now();
        CoinMetricsOnchainRawSnapshot snapshot = coinMetricsOnchainClient.fetchLatestMetric(assetType.onchainAssetCode(), metricType);

        OnchainSnapshotRawEntity existingEntity = snapshot.sourceEventTime() == null
                ? null
                : onchainSnapshotRawRepository
                        .findTopBySourceAndAssetCodeAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                                COIN_METRICS_SOURCE,
                                assetType.onchainAssetCode(),
                                metricType,
                                snapshot.sourceEventTime()
                        )
                        .orElse(null);

        if (existingEntity == null) {
            OnchainSnapshotRawEntity entity = OnchainSnapshotRawEntity.builder()
                                                                     .source(COIN_METRICS_SOURCE)
                                                                     .assetCode(assetType.onchainAssetCode())
                                                                     .metricType(metricType)
                                                                     .sourceEventTime(snapshot.sourceEventTime())
                                                                     .collectedTime(collectedTime)
                                                                     .validationStatus(snapshot.validation().status())
                                                                     .validationDetails(snapshot.validation().details())
                                                                     .metricValue(snapshot.metricValue())
                                                                     .rawPayload(snapshot.rawPayload())
                                                                     .build();
            return onchainSnapshotRawRepository.save(entity);
        }

        existingEntity.refreshFromIngestion(
                collectedTime,
                snapshot.validation().status(),
                snapshot.validation().details(),
                snapshot.metricValue(),
                snapshot.rawPayload()
        );
        return existingEntity;
    }
}

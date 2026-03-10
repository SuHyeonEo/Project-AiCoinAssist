package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.onchain.dto.OnchainFactSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.domain.onchain.repository.OnchainSnapshotRawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OnchainFactSnapshotService {

    private final OnchainSnapshotRawRepository onchainSnapshotRawRepository;

    public OnchainFactSnapshot create(String symbol) {
        AssetType assetType = AssetType.fromSymbol(symbol);

        OnchainSnapshotRawEntity activeAddressRaw = latestValidRaw(assetType.onchainAssetCode(), OnchainMetricType.ACTIVE_ADDRESS_COUNT);
        OnchainSnapshotRawEntity transactionCountRaw = latestValidRaw(assetType.onchainAssetCode(), OnchainMetricType.TRANSACTION_COUNT);
        OnchainSnapshotRawEntity marketCapRaw = latestValidRaw(assetType.onchainAssetCode(), OnchainMetricType.MARKET_CAP_USD);

        Instant snapshotTime = latestSourceEventTime(activeAddressRaw, transactionCountRaw, marketCapRaw);

        return new OnchainFactSnapshot(
                symbol,
                assetType.onchainAssetCode(),
                snapshotTime,
                activeAddressRaw.getSourceEventTime(),
                transactionCountRaw.getSourceEventTime(),
                marketCapRaw.getSourceEventTime(),
                buildSourceDataVersion(activeAddressRaw, transactionCountRaw, marketCapRaw),
                activeAddressRaw.getMetricValue(),
                transactionCountRaw.getMetricValue(),
                marketCapRaw.getMetricValue()
        );
    }

    private OnchainSnapshotRawEntity latestValidRaw(String assetCode, OnchainMetricType metricType) {
        OnchainSnapshotRawEntity rawEntity = onchainSnapshotRawRepository
                .findTopByAssetCodeAndMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(assetCode, metricType)
                .orElseThrow(() -> new IllegalStateException("No on-chain raw snapshot found for asset=%s metric=%s.".formatted(assetCode, metricType)));

        if (rawEntity.getValidationStatus() != RawDataValidationStatus.VALID) {
            throw new IllegalStateException("On-chain raw snapshot is invalid for asset=%s metric=%s: %s"
                                                    .formatted(assetCode, metricType, rawEntity.getValidationDetails()));
        }

        return rawEntity;
    }

    private Instant latestSourceEventTime(
            OnchainSnapshotRawEntity activeAddressRaw,
            OnchainSnapshotRawEntity transactionCountRaw,
            OnchainSnapshotRawEntity marketCapRaw
    ) {
        Instant latest = activeAddressRaw.getSourceEventTime();
        if (transactionCountRaw.getSourceEventTime().isAfter(latest)) {
            latest = transactionCountRaw.getSourceEventTime();
        }
        if (marketCapRaw.getSourceEventTime().isAfter(latest)) {
            latest = marketCapRaw.getSourceEventTime();
        }
        return latest;
    }

    private String buildSourceDataVersion(
            OnchainSnapshotRawEntity activeAddressRaw,
            OnchainSnapshotRawEntity transactionCountRaw,
            OnchainSnapshotRawEntity marketCapRaw
    ) {
        return "activeAddressSourceEventTime=" + activeAddressRaw.getSourceEventTime()
                + ";transactionCountSourceEventTime=" + transactionCountRaw.getSourceEventTime()
                + ";marketCapSourceEventTime=" + marketCapRaw.getSourceEventTime();
    }
}

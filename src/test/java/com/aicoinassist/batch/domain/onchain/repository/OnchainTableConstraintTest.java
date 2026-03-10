package com.aicoinassist.batch.domain.onchain.repository;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainSnapshotRawEntity;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class OnchainTableConstraintTest {

    @Autowired
    private OnchainSnapshotRawRepository onchainSnapshotRawRepository;

    @Autowired
    private OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    @Test
    void onchainSnapshotRawRejectsDuplicateSourceAssetMetricAndSourceEventTime() {
        Instant sourceEventTime = Instant.parse("2026-03-09T00:00:00Z");

        onchainSnapshotRawRepository.saveAndFlush(rawEntity(sourceEventTime, OnchainMetricType.ACTIVE_ADDRESS_COUNT, "815234.00000000"));

        assertThatThrownBy(() -> onchainSnapshotRawRepository.saveAndFlush(
                rawEntity(sourceEventTime, OnchainMetricType.ACTIVE_ADDRESS_COUNT, "815235.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onchainFactSnapshotRejectsDuplicateSymbolAndSnapshotTime() {
        Instant snapshotTime = Instant.parse("2026-03-09T00:00:00Z");

        onchainFactSnapshotRepository.saveAndFlush(snapshotEntity(snapshotTime, "815234.00000000"));

        assertThatThrownBy(() -> onchainFactSnapshotRepository.saveAndFlush(
                snapshotEntity(snapshotTime, "815235.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private OnchainSnapshotRawEntity rawEntity(Instant sourceEventTime, OnchainMetricType metricType, String value) {
        return OnchainSnapshotRawEntity.builder()
                                       .source("COIN_METRICS")
                                       .assetCode("btc")
                                       .metricType(metricType)
                                       .sourceEventTime(sourceEventTime)
                                       .collectedTime(sourceEventTime.plusSeconds(60))
                                       .validationStatus(RawDataValidationStatus.VALID)
                                       .metricValue(new BigDecimal(value))
                                       .rawPayload("{\"value\":\"" + value + "\"}")
                                       .build();
    }

    private OnchainFactSnapshotEntity snapshotEntity(Instant snapshotTime, String activeAddressCount) {
        return OnchainFactSnapshotEntity.builder()
                                        .symbol("BTCUSDT")
                                        .assetCode("btc")
                                        .snapshotTime(snapshotTime)
                                        .activeAddressSourceEventTime(snapshotTime)
                                        .transactionCountSourceEventTime(snapshotTime)
                                        .marketCapSourceEventTime(snapshotTime)
                                        .sourceDataVersion("activeAddressSourceEventTime=" + snapshotTime + ";transactionCountSourceEventTime=" + snapshotTime + ";marketCapSourceEventTime=" + snapshotTime)
                                        .activeAddressCount(new BigDecimal(activeAddressCount))
                                        .transactionCount(new BigDecimal("412345.00000000"))
                                        .marketCapUsd(new BigDecimal("1712345678900.00000000"))
                                        .build();
    }
}

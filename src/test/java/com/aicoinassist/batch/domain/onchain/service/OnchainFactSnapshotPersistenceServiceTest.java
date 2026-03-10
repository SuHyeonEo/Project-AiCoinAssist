package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.onchain.dto.OnchainFactSnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainFactSnapshotRepository;
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
class OnchainFactSnapshotPersistenceServiceTest {

    @Mock
    private OnchainFactSnapshotService onchainFactSnapshotService;

    @Mock
    private OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshotWhenKeyMatches() {
        OnchainFactSnapshotPersistenceService service = new OnchainFactSnapshotPersistenceService(
                onchainFactSnapshotService,
                onchainFactSnapshotRepository
        );

        OnchainFactSnapshot snapshot = snapshot();
        OnchainFactSnapshotEntity existingEntity = OnchainFactSnapshotEntity.builder()
                                                                           .symbol(snapshot.symbol())
                                                                           .assetCode(snapshot.assetCode())
                                                                           .snapshotTime(snapshot.snapshotTime())
                                                                           .activeAddressSourceEventTime(snapshot.activeAddressSourceEventTime().minusSeconds(60))
                                                                           .transactionCountSourceEventTime(snapshot.transactionCountSourceEventTime().minusSeconds(60))
                                                                           .marketCapSourceEventTime(snapshot.marketCapSourceEventTime().minusSeconds(60))
                                                                           .sourceDataVersion("old")
                                                                           .activeAddressCount(new BigDecimal("800000.00000000"))
                                                                           .transactionCount(new BigDecimal("400000.00000000"))
                                                                           .marketCapUsd(new BigDecimal("1700000000000.00000000"))
                                                                           .build();

        when(onchainFactSnapshotService.create("BTCUSDT")).thenReturn(snapshot);
        when(onchainFactSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc("BTCUSDT", snapshot.snapshotTime()))
                .thenReturn(Optional.of(existingEntity));

        OnchainFactSnapshotEntity result = service.createAndSave("BTCUSDT");

        verify(onchainFactSnapshotRepository, never()).save(any(OnchainFactSnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getTransactionCount()).isEqualByComparingTo("412345.00000000");
    }

    @Test
    void createAndSavePersistsNewSnapshotWhenKeyDoesNotExist() {
        OnchainFactSnapshotPersistenceService service = new OnchainFactSnapshotPersistenceService(
                onchainFactSnapshotService,
                onchainFactSnapshotRepository
        );

        OnchainFactSnapshot snapshot = snapshot();

        when(onchainFactSnapshotService.create("BTCUSDT")).thenReturn(snapshot);
        when(onchainFactSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc("BTCUSDT", snapshot.snapshotTime()))
                .thenReturn(Optional.empty());
        when(onchainFactSnapshotRepository.save(any(OnchainFactSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OnchainFactSnapshotEntity result = service.createAndSave("BTCUSDT");

        assertThat(result.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(result.getMarketCapUsd()).isEqualByComparingTo("1712345678900.00000000");
    }

    private OnchainFactSnapshot snapshot() {
        return new OnchainFactSnapshot(
                "BTCUSDT",
                "btc",
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                "activeAddressSourceEventTime=2026-03-09T00:00:00Z;transactionCountSourceEventTime=2026-03-09T00:00:00Z;marketCapSourceEventTime=2026-03-09T00:00:00Z",
                new BigDecimal("815234.00000000"),
                new BigDecimal("412345.00000000"),
                new BigDecimal("1712345678900.00000000")
        );
    }
}

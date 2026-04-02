package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketContextSnapshotRepository;
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
class MarketContextSnapshotPersistenceServiceTest {

    @Mock
    private MarketContextSnapshotService marketContextSnapshotService;

    @Mock
    private MarketContextSnapshotRepository marketContextSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshotWhenKeyMatches() {
        MarketContextSnapshotPersistenceService service = new MarketContextSnapshotPersistenceService(
                marketContextSnapshotService,
                marketContextSnapshotRepository
        );

        MarketContextSnapshot snapshot = snapshot();
        MarketContextSnapshotEntity existingEntity = MarketContextSnapshotEntity.builder()
                                                                                .symbol("BTCUSDT")
                                                                                .snapshotTime(snapshot.snapshotTime())
                                                                                .openInterestSourceEventTime(snapshot.openInterestSourceEventTime().minusSeconds(60))
                                                                                .premiumIndexSourceEventTime(snapshot.premiumIndexSourceEventTime().minusSeconds(60))
                                                                                .sourceDataVersion("old-version")
                                                                                .openInterest(new BigDecimal("10000"))
                                                                                .markPrice(new BigDecimal("87000"))
                                                                                .indexPrice(new BigDecimal("86990"))
                                                                                .lastFundingRate(new BigDecimal("0.0001"))
                                                                                .nextFundingTime(Instant.parse("2026-03-10T08:00:00Z"))
                                                                                .markIndexBasisRate(new BigDecimal("0.01"))
                                                                                .build();

        when(marketContextSnapshotService.create("BTCUSDT")).thenReturn(snapshot);
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                snapshot.snapshotTime()
        )).thenReturn(Optional.of(existingEntity));

        MarketContextSnapshotEntity result = service.createAndSave("BTCUSDT");

        verify(marketContextSnapshotRepository, never()).save(any(MarketContextSnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getSourceDataVersion()).isEqualTo(snapshot.sourceDataVersion());
        assertThat(existingEntity.getOpenInterest()).isEqualByComparingTo("12345.67890000");
        assertThat(existingEntity.getMarkIndexBasisRate()).isEqualByComparingTo("0.02297893");
    }

    @Test
    void createAndSavePersistsNewSnapshotWhenKeyDoesNotExist() {
        MarketContextSnapshotPersistenceService service = new MarketContextSnapshotPersistenceService(
                marketContextSnapshotService,
                marketContextSnapshotRepository
        );

        MarketContextSnapshot snapshot = snapshot();

        when(marketContextSnapshotService.create("BTCUSDT")).thenReturn(snapshot);
        when(marketContextSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc(
                "BTCUSDT",
                snapshot.snapshotTime()
        )).thenReturn(Optional.empty());
        when(marketContextSnapshotRepository.save(any(MarketContextSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketContextSnapshotEntity result = service.createAndSave("BTCUSDT");

        assertThat(result.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(result.getSnapshotTime()).isEqualTo(snapshot.snapshotTime());
        assertThat(result.getOpenInterest()).isEqualByComparingTo("12345.67890000");
        assertThat(result.getLastFundingRate()).isEqualByComparingTo("0.00025000");
    }

    private MarketContextSnapshot snapshot() {
        return new MarketContextSnapshot(
                "BTCUSDT",
                Instant.parse("2026-03-10T00:59:30Z"),
                Instant.parse("2026-03-10T00:59:00Z"),
                Instant.parse("2026-03-10T00:59:30Z"),
                "openInterestSourceEventTime=2026-03-10T00:59:00Z;premiumIndexSourceEventTime=2026-03-10T00:59:30Z;nextFundingTime=2026-03-10T08:00:00Z",
                new BigDecimal("12345.67890000"),
                new BigDecimal("87500.12000000"),
                new BigDecimal("87480.02000000"),
                new BigDecimal("0.00025000"),
                Instant.parse("2026-03-10T08:00:00Z"),
                new BigDecimal("0.02297893")
        );
    }
}

package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextSnapshotRepository;
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
class MacroContextSnapshotPersistenceServiceTest {

    @Mock
    private MacroContextSnapshotService macroContextSnapshotService;

    @Mock
    private MacroContextSnapshotRepository macroContextSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshotWhenKeyMatches() {
        MacroContextSnapshotPersistenceService service = new MacroContextSnapshotPersistenceService(
                macroContextSnapshotService,
                macroContextSnapshotRepository
        );

        MacroContextSnapshot snapshot = snapshot();
        MacroContextSnapshotEntity existingEntity = MacroContextSnapshotEntity.builder()
                                                                             .snapshotTime(snapshot.snapshotTime())
                                                                             .dxyObservationDate(snapshot.dxyObservationDate().minusDays(1))
                                                                             .us10yYieldObservationDate(snapshot.us10yYieldObservationDate().minusDays(1))
                                                                             .usdKrwObservationDate(snapshot.usdKrwObservationDate().minusDays(1))
                                                                             .sourceDataVersion("old")
                                                                             .dxyProxyValue(new BigDecimal("118.00000000"))
                                                                             .us10yYieldValue(new BigDecimal("4.00000000"))
                                                                             .usdKrwValue(new BigDecimal("1450.00000000"))
                                                                             .build();

        when(macroContextSnapshotService.create()).thenReturn(snapshot);
        when(macroContextSnapshotRepository.findTopBySnapshotTimeOrderByIdDesc(snapshot.snapshotTime()))
                .thenReturn(Optional.of(existingEntity));

        MacroContextSnapshotEntity result = service.createAndSave();

        verify(macroContextSnapshotRepository, never()).save(any(MacroContextSnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getDxyProxyValue()).isEqualByComparingTo("119.84210000");
        assertThat(existingEntity.getUsdKrwObservationDate()).isEqualTo(LocalDate.parse("2026-03-09"));
    }

    @Test
    void createAndSavePersistsNewSnapshotWhenKeyDoesNotExist() {
        MacroContextSnapshotPersistenceService service = new MacroContextSnapshotPersistenceService(
                macroContextSnapshotService,
                macroContextSnapshotRepository
        );

        MacroContextSnapshot snapshot = snapshot();

        when(macroContextSnapshotService.create()).thenReturn(snapshot);
        when(macroContextSnapshotRepository.findTopBySnapshotTimeOrderByIdDesc(snapshot.snapshotTime()))
                .thenReturn(Optional.empty());
        when(macroContextSnapshotRepository.save(any(MacroContextSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MacroContextSnapshotEntity result = service.createAndSave();

        assertThat(result.getSnapshotTime()).isEqualTo(snapshot.snapshotTime());
        assertThat(result.getUs10yYieldValue()).isEqualByComparingTo("4.12000000");
    }

    private MacroContextSnapshot snapshot() {
        return new MacroContextSnapshot(
                Instant.parse("2026-03-10T00:00:00Z"),
                LocalDate.parse("2026-03-10"),
                LocalDate.parse("2026-03-10"),
                LocalDate.parse("2026-03-09"),
                "dxyProxyDate=2026-03-10;us10yYieldDate=2026-03-10;usdKrwDate=2026-03-09",
                new BigDecimal("119.84210000"),
                new BigDecimal("4.12000000"),
                new BigDecimal("1453.22000000")
        );
    }
}

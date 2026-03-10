package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextSnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MacroContextSnapshotPersistenceService {

    private final MacroContextSnapshotService macroContextSnapshotService;
    private final MacroContextSnapshotRepository macroContextSnapshotRepository;

    @Transactional
    public MacroContextSnapshotEntity createAndSave() {
        MacroContextSnapshot snapshot = macroContextSnapshotService.create();

        MacroContextSnapshotEntity existingEntity = macroContextSnapshotRepository
                .findTopBySnapshotTimeOrderByIdDesc(snapshot.snapshotTime())
                .orElse(null);

        if (existingEntity == null) {
            MacroContextSnapshotEntity entity = MacroContextSnapshotEntity.builder()
                                                                         .snapshotTime(snapshot.snapshotTime())
                                                                         .dxyObservationDate(snapshot.dxyObservationDate())
                                                                         .us10yYieldObservationDate(snapshot.us10yYieldObservationDate())
                                                                         .usdKrwObservationDate(snapshot.usdKrwObservationDate())
                                                                         .sourceDataVersion(snapshot.sourceDataVersion())
                                                                         .dxyProxyValue(snapshot.dxyProxyValue())
                                                                         .us10yYieldValue(snapshot.us10yYieldValue())
                                                                         .usdKrwValue(snapshot.usdKrwValue())
                                                                         .build();
            return macroContextSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                snapshot.dxyObservationDate(),
                snapshot.us10yYieldObservationDate(),
                snapshot.usdKrwObservationDate(),
                snapshot.sourceDataVersion(),
                snapshot.dxyProxyValue(),
                snapshot.us10yYieldValue(),
                snapshot.usdKrwValue()
        );
        return existingEntity;
    }
}

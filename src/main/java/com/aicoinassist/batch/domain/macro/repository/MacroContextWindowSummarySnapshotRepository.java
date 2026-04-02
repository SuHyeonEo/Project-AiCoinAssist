package com.aicoinassist.batch.domain.macro.repository;

import com.aicoinassist.batch.domain.macro.entity.MacroContextWindowSummarySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MacroContextWindowSummarySnapshotRepository extends JpaRepository<MacroContextWindowSummarySnapshotEntity, Long> {

    Optional<MacroContextWindowSummarySnapshotEntity> findTopByWindowTypeAndWindowEndTimeOrderByIdDesc(
            String windowType,
            Instant windowEndTime
    );
}

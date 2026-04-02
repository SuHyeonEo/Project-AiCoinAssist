package com.aicoinassist.batch.domain.macro.repository;

import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MacroContextSnapshotRepository extends JpaRepository<MacroContextSnapshotEntity, Long> {

    Optional<MacroContextSnapshotEntity> findTopBySnapshotTimeOrderByIdDesc(Instant snapshotTime);

    Optional<MacroContextSnapshotEntity> findTopBySnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(Instant snapshotTime);

    List<MacroContextSnapshotEntity> findAllBySnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
            Instant startTime,
            Instant endTime
    );
}

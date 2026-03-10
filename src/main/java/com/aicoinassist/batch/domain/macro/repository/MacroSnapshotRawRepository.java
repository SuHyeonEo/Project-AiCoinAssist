package com.aicoinassist.batch.domain.macro.repository;

import com.aicoinassist.batch.domain.macro.entity.MacroSnapshotRawEntity;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MacroSnapshotRawRepository extends JpaRepository<MacroSnapshotRawEntity, Long> {

    Optional<MacroSnapshotRawEntity> findTopBySourceAndMetricTypeAndObservationDateOrderByCollectedTimeDescIdDesc(
            String source,
            MacroMetricType metricType,
            LocalDate observationDate
    );

    Optional<MacroSnapshotRawEntity> findTopByMetricTypeOrderByObservationDateDescCollectedTimeDescIdDesc(
            MacroMetricType metricType
    );
}

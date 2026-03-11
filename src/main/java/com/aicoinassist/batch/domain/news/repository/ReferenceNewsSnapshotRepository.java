package com.aicoinassist.batch.domain.news.repository;

import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReferenceNewsSnapshotRepository extends JpaRepository<ReferenceNewsSnapshotEntity, Long> {

    Optional<ReferenceNewsSnapshotEntity> findTopByScopeAndSnapshotDateOrderByIdDesc(String scope, LocalDate snapshotDate);

    Optional<ReferenceNewsSnapshotEntity> findTopByScopeOrderBySnapshotDateDescIdDesc(String scope);
}

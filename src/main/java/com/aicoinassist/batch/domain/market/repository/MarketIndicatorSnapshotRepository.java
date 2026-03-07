package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndicatorSnapshotRepository extends JpaRepository<MarketIndicatorSnapshotEntity, Long> {
}
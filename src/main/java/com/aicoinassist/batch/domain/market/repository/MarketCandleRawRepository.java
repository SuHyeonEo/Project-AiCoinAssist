package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketCandleRawRepository extends JpaRepository<MarketCandleRawEntity, Long> {
}

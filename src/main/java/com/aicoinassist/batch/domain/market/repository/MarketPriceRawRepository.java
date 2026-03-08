package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceRawRepository extends JpaRepository<MarketPriceRawEntity, Long> {
}

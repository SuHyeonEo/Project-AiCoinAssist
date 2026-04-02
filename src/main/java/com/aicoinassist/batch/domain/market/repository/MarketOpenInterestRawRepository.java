package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketOpenInterestRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketOpenInterestRawRepository extends JpaRepository<MarketOpenInterestRawEntity, Long> {

    Optional<MarketOpenInterestRawEntity> findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
            String source,
            String symbol,
            Instant sourceEventTime
    );
}

package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketPremiumIndexRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketPremiumIndexRawRepository extends JpaRepository<MarketPremiumIndexRawEntity, Long> {

    Optional<MarketPremiumIndexRawEntity> findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
            String source,
            String symbol,
            Instant sourceEventTime
    );
}

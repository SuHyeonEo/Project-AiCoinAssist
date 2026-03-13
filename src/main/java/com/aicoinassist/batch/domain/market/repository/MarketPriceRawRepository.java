package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MarketPriceRawRepository extends JpaRepository<MarketPriceRawEntity, Long> {

    Optional<MarketPriceRawEntity> findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
            String source,
            String symbol,
            Instant sourceEventTime
    );

    Optional<MarketPriceRawEntity> findTopBySymbolAndValidationStatusOrderBySourceEventTimeDescIdDesc(
            String symbol,
            RawDataValidationStatus validationStatus
    );
}

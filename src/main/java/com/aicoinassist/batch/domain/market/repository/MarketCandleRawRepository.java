package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface MarketCandleRawRepository extends JpaRepository<MarketCandleRawEntity, Long> {

    List<MarketCandleRawEntity> findAllBySourceAndSymbolAndIntervalValueAndOpenTimeIn(
            String source,
            String symbol,
            String intervalValue,
            Collection<Instant> openTimes
    );
}

package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MarketCandleRawRepository extends JpaRepository<MarketCandleRawEntity, Long> {

    Optional<MarketCandleRawEntity> findTopBySourceAndSymbolAndIntervalValueAndOpenTimeOrderByCollectedTimeDescIdDesc(
            String source,
            String symbol,
            String intervalValue,
            Instant openTime
    );

    Optional<MarketCandleRawEntity> findTopBySymbolAndIntervalValueAndValidationStatusOrderByOpenTimeDescIdDesc(
            String symbol,
            String intervalValue,
            RawDataValidationStatus validationStatus
    );

    long countBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatus(
            String symbol,
            String intervalValue,
            Instant openTimeFrom,
            Instant openTimeTo,
            RawDataValidationStatus validationStatus
    );

    List<MarketCandleRawEntity> findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
            String symbol,
            String intervalValue,
            Instant openTimeFrom,
            Instant openTimeTo,
            RawDataValidationStatus validationStatus
    );
}

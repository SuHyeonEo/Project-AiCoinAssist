package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelZoneSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketCandidateLevelZoneSnapshotServiceTest {

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Test
    void createAllClustersNearbySupportAndResistanceLevelsIntoZones() {
        MarketCandidateLevelZoneSnapshotService service = new MarketCandidateLevelZoneSnapshotService(marketCandleRawRepository);
        List<MarketCandidateLevelSnapshotEntity> levelEntities = List.of(
                level("SUPPORT", "MA20", "MOVING_AVERAGE", "87000.00", "0.64428571", 2, 1),
                level("SUPPORT", "PIVOT_LOW", "PIVOT_LEVEL", "86850.00", "0.80285714", 4, 2),
                level("SUPPORT", "MA60", "MOVING_AVERAGE", "86000.00", "0.78285714", 3, 2),
                level("RESISTANCE", "BB_UPPER", "BOLLINGER_BAND", "88500.00", "0.63857143", 1, 1),
                level("RESISTANCE", "PIVOT_HIGH", "PIVOT_LEVEL", "88620.00", "0.81285714", 3, 2)
        );
        when(marketCandleRawRepository.findAllBySourceAndSymbolAndIntervalValueAndValidationStatusAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                "BINANCE",
                "BTCUSDT",
                "1h",
                RawDataValidationStatus.VALID,
                Instant.parse("2026-02-24T00:59:59Z"),
                Instant.parse("2026-03-10T00:59:59Z")
        )).thenReturn(recentCandles());

        List<MarketCandidateLevelZoneSnapshot> zones = service.createAll(levelEntities);

        assertThat(zones).hasSize(3);

        MarketCandidateLevelZoneSnapshot supportZone = zones.stream()
                                                            .filter(zone -> zone.zoneType() == MarketCandidateLevelType.SUPPORT)
                                                            .filter(zone -> zone.zoneRank() == 1)
                                                            .findFirst()
                                                            .orElseThrow();
        assertThat(supportZone.zoneLow()).isEqualByComparingTo("86850.00000000");
        assertThat(supportZone.zoneHigh()).isEqualByComparingTo("87000.00000000");
        assertThat(supportZone.strongestLevelLabel()).isEqualTo(MarketCandidateLevelLabel.PIVOT_LOW);
        assertThat(supportZone.includedLevelLabels()).containsExactly(
                MarketCandidateLevelLabel.PIVOT_LOW,
                MarketCandidateLevelLabel.MA20
        );
        assertThat(supportZone.interactionType()).isEqualTo(MarketCandidateLevelZoneInteractionType.ABOVE_ZONE);
        assertThat(supportZone.recentTestCount()).isGreaterThanOrEqualTo(1);
        assertThat(supportZone.recentRejectionCount()).isGreaterThanOrEqualTo(1);
        assertThat(supportZone.levelCount()).isEqualTo(2);
        assertThat(supportZone.triggerFacts()).anySatisfy(fact -> assertThat(fact).contains("SUPPORT zone spans"));
        assertThat(supportZone.triggerFacts()).anySatisfy(fact -> assertThat(fact).contains("Recent tests"));

        MarketCandidateLevelZoneSnapshot resistanceZone = zones.stream()
                                                               .filter(zone -> zone.zoneType() == MarketCandidateLevelType.RESISTANCE)
                                                               .findFirst()
                                                               .orElseThrow();
        assertThat(resistanceZone.zoneLow()).isEqualByComparingTo("88500.00000000");
        assertThat(resistanceZone.zoneHigh()).isEqualByComparingTo("88620.00000000");
        assertThat(resistanceZone.strongestSourceType()).isEqualTo(MarketCandidateLevelSourceType.PIVOT_LEVEL);
        assertThat(resistanceZone.interactionType()).isEqualTo(MarketCandidateLevelZoneInteractionType.BELOW_ZONE);
        assertThat(resistanceZone.levelCount()).isEqualTo(2);
    }

    private List<MarketCandleRawEntity> recentCandles() {
        return List.of(
                candle("2026-03-08T00:59:59Z", "87200", "87800", "86920", "87250"),
                candle("2026-03-08T04:59:59Z", "88300", "88640", "88220", "88410"),
                candle("2026-03-09T00:59:59Z", "87100", "87600", "86880", "87120")
        );
    }

    private MarketCandidateLevelSnapshotEntity level(
            String levelType,
            String levelLabel,
            String sourceType,
            String levelPrice,
            String strengthScore,
            int reactionCount,
            int clusterSize
    ) {
        return MarketCandidateLevelSnapshotEntity.builder()
                                                 .symbol("BTCUSDT")
                                                 .intervalValue("1h")
                                                 .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                                                 .referenceTime(Instant.parse("2026-03-09T23:59:59Z"))
                                                 .levelType(levelType)
                                                 .levelLabel(levelLabel)
                                                 .sourceType(sourceType)
                                                 .currentPrice(new BigDecimal("87500.00000000"))
                                                 .levelPrice(new BigDecimal(levelPrice))
                                                 .distanceFromCurrent(new BigDecimal("0.00571429"))
                                                 .strengthScore(new BigDecimal(strengthScore))
                                                 .reactionCount(reactionCount)
                                                 .clusterSize(clusterSize)
                                                 .rationale("candidate")
                                                 .triggerFactsPayload("[\"Current price 87500 vs " + levelLabel + " " + levelPrice + "\",\"SUPPORT distance 0.57%\"]")
                                                 .sourceDataVersion("basis-key;" + levelType + ";" + levelLabel)
                                                 .build();
    }

    private MarketCandleRawEntity candle(
            String openTime,
            String openPrice,
            String highPrice,
            String lowPrice,
            String closePrice
    ) {
        Instant start = Instant.parse(openTime);
        return MarketCandleRawEntity.builder()
                                    .source("BINANCE")
                                    .symbol("BTCUSDT")
                                    .intervalValue("1h")
                                    .openTime(start)
                                    .closeTime(start.plusSeconds(3599))
                                    .openPrice(new BigDecimal(openPrice))
                                    .highPrice(new BigDecimal(highPrice))
                                    .lowPrice(new BigDecimal(lowPrice))
                                    .closePrice(new BigDecimal(closePrice))
                                    .volume(new BigDecimal("100"))
                                    .collectedTime(start.plusSeconds(60))
                                    .validationStatus(RawDataValidationStatus.VALID)
                                    .rawPayload("[]")
                                    .build();
    }
}

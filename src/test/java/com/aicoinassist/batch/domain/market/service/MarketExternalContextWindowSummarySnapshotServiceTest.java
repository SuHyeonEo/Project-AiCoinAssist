package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketExternalContextWindowSummarySnapshotServiceTest {

    @Mock
    private MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;

    @InjectMocks
    private MarketExternalContextWindowSummarySnapshotService service;

    @Test
    void createBuildsRepresentativeWindowSummaryFromExternalSnapshots() {
        MarketExternalContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:30Z"),
                "1.33333333",
                "HEADWIND",
                "HIGH",
                "v3"
        );
        MarketExternalContextSnapshotEntity priorSnapshot = snapshot(
                Instant.parse("2026-03-05T00:59:30Z"),
                "0.90000000",
                "CAUTIONARY",
                "MEDIUM",
                "v2"
        );
        MarketExternalContextSnapshotEntity olderSnapshot = snapshot(
                Instant.parse("2026-03-01T00:59:30Z"),
                "0.70000000",
                "SUPPORTIVE",
                "LOW",
                "v1"
        );

        when(marketExternalContextSnapshotRepository
                .findAllBySymbolAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        "BTCUSDT",
                        Instant.parse("2026-03-02T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z")
                ))
                .thenReturn(List.of(priorSnapshot, currentSnapshot));

        var summary = service.create(currentSnapshot, MarketWindowType.LAST_7D);

        assertThat(summary.symbol()).isEqualTo("BTCUSDT");
        assertThat(summary.windowType()).isEqualTo(MarketWindowType.LAST_7D);
        assertThat(summary.sampleCount()).isEqualTo(2);
        assertThat(summary.averageCompositeRiskScore()).isEqualByComparingTo("1.11666667");
        assertThat(summary.currentCompositeRiskVsAverage()).isEqualByComparingTo("0.19402984");
        assertThat(summary.supportiveDominanceSampleCount()).isZero();
        assertThat(summary.cautionaryDominanceSampleCount()).isEqualTo(1);
        assertThat(summary.headwindDominanceSampleCount()).isEqualTo(1);
        assertThat(summary.highSeveritySampleCount()).isEqualTo(1);
        assertThat(summary.sourceDataVersion()).contains("windowType=LAST_7D");
        assertThat(summary.sourceDataVersion()).contains("sampleCount=2");
        assertThat(olderSnapshot.getCompositeRiskScore()).isEqualByComparingTo("0.70000000");
    }

    private MarketExternalContextSnapshotEntity snapshot(
            Instant snapshotTime,
            String compositeRiskScore,
            String dominantDirection,
            String highestSeverity,
            String sourceDataVersion
    ) {
        return MarketExternalContextSnapshotEntity.builder()
                .symbol("BTCUSDT")
                .snapshotTime(snapshotTime)
                .derivativeSnapshotTime(snapshotTime)
                .macroSnapshotTime(snapshotTime)
                .sentimentSnapshotTime(snapshotTime)
                .onchainSnapshotTime(snapshotTime)
                .sourceDataVersion(sourceDataVersion)
                .compositeRiskScore(new BigDecimal(compositeRiskScore))
                .dominantDirection(dominantDirection)
                .highestSeverity(highestSeverity)
                .supportiveSignalCount(0)
                .cautionarySignalCount(1)
                .headwindSignalCount(1)
                .primarySignalCategory("MACRO")
                .primarySignalTitle("Dollar strength regime")
                .primarySignalDetail("DXY remains firm.")
                .regimeSignalsPayload("[]")
                .build();
    }
}

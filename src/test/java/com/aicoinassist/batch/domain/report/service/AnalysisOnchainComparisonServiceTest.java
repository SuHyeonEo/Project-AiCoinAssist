package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainFactSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisOnchainComparisonServiceTest {

    @Mock
    private OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    @Test
    void buildFactsReturnsShortTermOnchainComparisons() {
        AnalysisOnchainComparisonService service = new AnalysisOnchainComparisonService(onchainFactSnapshotRepository);
        OnchainFactSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:00:00Z"),
                "1050000.00000000",
                "525000.00000000",
                "1700000000000.00000000"
        );

        when(onchainFactSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-08T23:59:59.999999999Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:00:00Z"),
                "1000000.00000000",
                "500000.00000000",
                "1680000000000.00000000"
        )));
        when(onchainFactSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-08T00:00:00Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:00:00Z"),
                "1000000.00000000",
                "500000.00000000",
                "1680000000000.00000000"
        )));
        when(onchainFactSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-03-02T00:00:00Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-02T00:00:00Z"),
                "954545.45454545",
                "480000.00000000",
                "1670000000000.00000000"
        )));

        List<AnalysisOnchainComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).extracting(AnalysisOnchainComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.PREV_BATCH,
                                 AnalysisComparisonReference.D1,
                                 AnalysisComparisonReference.D7
                         );
        assertThat(facts.get(0).activeAddressChangeRate()).isEqualByComparingTo("0.05000000");
        assertThat(facts.get(0).transactionCountChangeRate()).isEqualByComparingTo("0.05000000");
        assertThat(facts.get(2).marketCapChangeRate()).isEqualByComparingTo("0.01796407");
    }

    private OnchainFactSnapshotEntity snapshot(
            Instant snapshotTime,
            String activeAddressCount,
            String transactionCount,
            String marketCapUsd
    ) {
        return OnchainFactSnapshotEntity.builder()
                                        .symbol("BTCUSDT")
                                        .assetCode("btc")
                                        .snapshotTime(snapshotTime)
                                        .activeAddressSourceEventTime(snapshotTime)
                                        .transactionCountSourceEventTime(snapshotTime)
                                        .marketCapSourceEventTime(snapshotTime)
                                        .sourceDataVersion("onchain-basis")
                                        .activeAddressCount(new BigDecimal(activeAddressCount))
                                        .transactionCount(new BigDecimal(transactionCount))
                                        .marketCapUsd(new BigDecimal(marketCapUsd))
                                        .build();
    }
}

package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
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
class AnalysisSentimentComparisonServiceTest {

    @Mock
    private SentimentSnapshotRepository sentimentSnapshotRepository;

    @Test
    void buildFactsReturnsShortTermSentimentComparisons() {
        AnalysisSentimentComparisonService service = new AnalysisSentimentComparisonService(sentimentSnapshotRepository);
        SentimentSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:00:00Z"),
                "72.00000000",
                "Greed"
        );

        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                Instant.parse("2026-03-08T23:59:59.999999999Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:00:00Z"),
                "68.00000000",
                "Neutral"
        )));
        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                Instant.parse("2026-03-08T00:00:00Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-08T00:00:00Z"),
                "68.00000000",
                "Neutral"
        )));
        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                Instant.parse("2026-03-02T00:00:00Z")
        )).thenReturn(Optional.of(snapshot(
                Instant.parse("2026-03-02T00:00:00Z"),
                "55.00000000",
                "Neutral"
        )));

        List<AnalysisSentimentComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).extracting(AnalysisSentimentComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.PREV_BATCH,
                                 AnalysisComparisonReference.D1,
                                 AnalysisComparisonReference.D7
                         );
        assertThat(facts.get(0).valueChange()).isEqualByComparingTo("4.00000000");
        assertThat(facts.get(0).valueChangeRate()).isEqualByComparingTo("0.05882353");
        assertThat(facts.get(0).classificationChanged()).isTrue();
    }

    private SentimentSnapshotEntity snapshot(Instant snapshotTime, String indexValue, String classification) {
        return SentimentSnapshotEntity.builder()
                                      .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                      .snapshotTime(snapshotTime)
                                      .sourceEventTime(snapshotTime)
                                      .sourceDataVersion("metricType=FEAR_GREED_INDEX;sourceEventTime=" + snapshotTime)
                                      .indexValue(new BigDecimal(indexValue))
                                      .classification(classification)
                                      .timeUntilUpdateSeconds(3600L)
                                      .build();
    }
}

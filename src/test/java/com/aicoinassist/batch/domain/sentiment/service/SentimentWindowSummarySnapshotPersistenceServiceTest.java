package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.dto.SentimentWindowSummarySnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentWindowSummarySnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private SentimentWindowSummarySnapshotService sentimentWindowSummarySnapshotService;

    @Mock
    private SentimentWindowSummarySnapshotRepository sentimentWindowSummarySnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSummaryWhenKeyMatches() {
        SentimentWindowSummarySnapshotPersistenceService service = new SentimentWindowSummarySnapshotPersistenceService(
                sentimentWindowSummarySnapshotService,
                sentimentWindowSummarySnapshotRepository
        );

        SentimentSnapshotEntity currentSnapshot = currentSnapshot();
        SentimentWindowSummarySnapshot summary = summary(MarketWindowType.LAST_7D);
        SentimentWindowSummarySnapshotEntity existingEntity = SentimentWindowSummarySnapshotEntity.builder()
                                                                                                  .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                                                                                  .windowType(MarketWindowType.LAST_7D.name())
                                                                                                  .windowStartTime(summary.windowStartTime())
                                                                                                  .windowEndTime(summary.windowEndTime())
                                                                                                  .sampleCount(5)
                                                                                                  .currentIndexValue(new BigDecimal("70.00000000"))
                                                                                                  .averageIndexValue(new BigDecimal("60.00000000"))
                                                                                                  .currentIndexVsAverage(new BigDecimal("0.16666667"))
                                                                                                  .currentClassification("Greed")
                                                                                                  .greedSampleCount(4)
                                                                                                  .fearSampleCount(0)
                                                                                                  .sourceDataVersion("old-version")
                                                                                                  .build();

        when(sentimentWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_7D))
                .thenReturn(summary);
        when(sentimentWindowSummarySnapshotRepository.findTopByMetricTypeAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                MarketWindowType.LAST_7D.name(),
                summary.windowEndTime()
        )).thenReturn(Optional.of(existingEntity));

        SentimentWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_7D);

        verify(sentimentWindowSummarySnapshotRepository, never()).save(any(SentimentWindowSummarySnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getAverageIndexValue()).isEqualByComparingTo("61.00000000");
        assertThat(existingEntity.getCurrentIndexVsAverage()).isEqualByComparingTo("0.18032787");
    }

    @Test
    void createAndSaveForReportTypeBuildsExpectedWindowSet() {
        SentimentWindowSummarySnapshotPersistenceService service = new SentimentWindowSummarySnapshotPersistenceService(
                sentimentWindowSummarySnapshotService,
                sentimentWindowSummarySnapshotRepository
        );

        SentimentSnapshotEntity currentSnapshot = currentSnapshot();
        when(sentimentWindowSummarySnapshotService.create(any(SentimentSnapshotEntity.class), any(MarketWindowType.class)))
                .thenAnswer(invocation -> summary(invocation.getArgument(1)));
        when(sentimentWindowSummarySnapshotRepository.findTopByMetricTypeAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                any(SentimentMetricType.class),
                any(String.class),
                any(Instant.class)
        )).thenReturn(Optional.empty());
        when(sentimentWindowSummarySnapshotRepository.save(any(SentimentWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<SentimentWindowSummarySnapshotEntity> entities = service.createAndSaveForReportType(
                currentSnapshot,
                AnalysisReportType.MID_TERM
        );

        assertThat(entities).extracting(SentimentWindowSummarySnapshotEntity::getWindowType)
                            .containsExactly(
                                    MarketWindowType.LAST_7D.name(),
                                    MarketWindowType.LAST_14D.name(),
                                    MarketWindowType.LAST_30D.name()
                            );
    }

    private SentimentSnapshotEntity currentSnapshot() {
        return SentimentSnapshotEntity.builder()
                                      .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                      .snapshotTime(Instant.parse("2026-03-09T00:00:00Z"))
                                      .sourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                      .sourceDataVersion("metricType=FEAR_GREED_INDEX;sourceEventTime=2026-03-09T00:00:00Z")
                                      .indexValue(new BigDecimal("72.00000000"))
                                      .classification("Greed")
                                      .timeUntilUpdateSeconds(3600L)
                                      .previousSnapshotTime(Instant.parse("2026-03-08T00:00:00Z"))
                                      .previousIndexValue(new BigDecimal("68.00000000"))
                                      .valueChange(new BigDecimal("4.00000000"))
                                      .valueChangeRate(new BigDecimal("0.05882353"))
                                      .classificationChanged(true)
                                      .build();
    }

    private SentimentWindowSummarySnapshot summary(MarketWindowType windowType) {
        return new SentimentWindowSummarySnapshot(
                SentimentMetricType.FEAR_GREED_INDEX,
                windowType,
                Instant.parse("2026-03-02T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                7,
                new BigDecimal("72.00000000"),
                new BigDecimal("61.00000000"),
                new BigDecimal("0.18032787"),
                "Greed",
                5,
                0,
                "metricType=FEAR_GREED_INDEX;windowType=" + windowType.name()
        );
    }
}

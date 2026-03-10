package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.sentiment.dto.SentimentSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentSnapshotPersistenceServiceTest {

    @Mock
    private SentimentSnapshotService sentimentSnapshotService;

    @Mock
    private SentimentSnapshotRepository sentimentSnapshotRepository;

    @Test
    void createAndSaveFearGreedSnapshotRefreshesExistingSnapshotWhenKeyMatches() {
        SentimentSnapshotPersistenceService service = new SentimentSnapshotPersistenceService(
                sentimentSnapshotService,
                sentimentSnapshotRepository
        );

        SentimentSnapshot snapshot = snapshot();
        SentimentSnapshotEntity existingEntity = SentimentSnapshotEntity.builder()
                                                                       .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                                                       .snapshotTime(snapshot.snapshotTime())
                                                                       .sourceEventTime(snapshot.sourceEventTime().minusSeconds(60))
                                                                       .sourceDataVersion("old")
                                                                       .indexValue(new BigDecimal("40.00000000"))
                                                                       .classification("Fear")
                                                                       .build();

        when(sentimentSnapshotService.createFearGreedSnapshot()).thenReturn(snapshot);
        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeOrderByIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                snapshot.snapshotTime()
        )).thenReturn(Optional.of(existingEntity));

        SentimentSnapshotEntity result = service.createAndSaveFearGreedSnapshot();

        verify(sentimentSnapshotRepository, never()).save(any(SentimentSnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getIndexValue()).isEqualByComparingTo("54.00000000");
        assertThat(existingEntity.getClassificationChanged()).isTrue();
    }

    @Test
    void createAndSaveFearGreedSnapshotPersistsNewSnapshotWhenKeyDoesNotExist() {
        SentimentSnapshotPersistenceService service = new SentimentSnapshotPersistenceService(
                sentimentSnapshotService,
                sentimentSnapshotRepository
        );

        SentimentSnapshot snapshot = snapshot();

        when(sentimentSnapshotService.createFearGreedSnapshot()).thenReturn(snapshot);
        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeOrderByIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                snapshot.snapshotTime()
        )).thenReturn(Optional.empty());
        when(sentimentSnapshotRepository.save(any(SentimentSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SentimentSnapshotEntity result = service.createAndSaveFearGreedSnapshot();

        assertThat(result.getMetricType()).isEqualTo(SentimentMetricType.FEAR_GREED_INDEX);
        assertThat(result.getSnapshotTime()).isEqualTo(snapshot.snapshotTime());
        assertThat(result.getValueChangeRate()).isEqualByComparingTo("0.35000000");
    }

    private SentimentSnapshot snapshot() {
        return new SentimentSnapshot(
                SentimentMetricType.FEAR_GREED_INDEX,
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-10T00:00:00Z"),
                "metricType=FEAR_GREED_INDEX;sourceEventTime=2026-03-10T00:00:00Z",
                new BigDecimal("54.00000000"),
                "Neutral",
                3600L,
                Instant.parse("2026-03-09T00:00:00Z"),
                new BigDecimal("40.00000000"),
                new BigDecimal("14.00000000"),
                new BigDecimal("0.35000000"),
                true
        );
    }
}

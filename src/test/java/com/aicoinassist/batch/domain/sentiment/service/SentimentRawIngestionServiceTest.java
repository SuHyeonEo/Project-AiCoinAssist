package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.sentiment.dto.FearGreedRawSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRawRepository;
import com.aicoinassist.batch.infrastructure.client.alternativeme.AlternativeMeSentimentClient;
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
class SentimentRawIngestionServiceTest {

    @Mock
    private AlternativeMeSentimentClient alternativeMeSentimentClient;

    @Mock
    private SentimentSnapshotRawRepository sentimentSnapshotRawRepository;

    @Test
    void ingestFearGreedRefreshesExistingRawWhenKeyMatches() {
        SentimentRawIngestionService service = new SentimentRawIngestionService(
                alternativeMeSentimentClient,
                sentimentSnapshotRawRepository
        );

        FearGreedRawSnapshot snapshot = rawSnapshot();
        SentimentSnapshotRawEntity existingEntity = SentimentSnapshotRawEntity.builder()
                                                                              .source("ALTERNATIVE_ME")
                                                                              .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                                                              .sourceEventTime(snapshot.sourceEventTime())
                                                                              .collectedTime(snapshot.sourceEventTime().plusSeconds(10))
                                                                              .validationStatus(snapshot.validation().status())
                                                                              .validationDetails("old")
                                                                              .indexValue(new BigDecimal("43"))
                                                                              .classification("Fear")
                                                                              .timeUntilUpdateSeconds(1200L)
                                                                              .rawPayload("{\"old\":true}")
                                                                              .build();

        when(alternativeMeSentimentClient.fetchLatestFearGreed()).thenReturn(snapshot);
        when(sentimentSnapshotRawRepository.findTopBySourceAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "ALTERNATIVE_ME",
                SentimentMetricType.FEAR_GREED_INDEX,
                snapshot.sourceEventTime()
        )).thenReturn(Optional.of(existingEntity));

        SentimentSnapshotRawEntity result = service.ingestFearGreed();

        verify(sentimentSnapshotRawRepository, never()).save(any(SentimentSnapshotRawEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getIndexValue()).isEqualByComparingTo("54.00000000");
        assertThat(existingEntity.getClassification()).isEqualTo("Neutral");
    }

    @Test
    void ingestFearGreedPersistsNewRawWhenKeyDoesNotExist() {
        SentimentRawIngestionService service = new SentimentRawIngestionService(
                alternativeMeSentimentClient,
                sentimentSnapshotRawRepository
        );

        FearGreedRawSnapshot snapshot = rawSnapshot();

        when(alternativeMeSentimentClient.fetchLatestFearGreed()).thenReturn(snapshot);
        when(sentimentSnapshotRawRepository.findTopBySourceAndMetricTypeAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "ALTERNATIVE_ME",
                SentimentMetricType.FEAR_GREED_INDEX,
                snapshot.sourceEventTime()
        )).thenReturn(Optional.empty());
        when(sentimentSnapshotRawRepository.save(any(SentimentSnapshotRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SentimentSnapshotRawEntity result = service.ingestFearGreed();

        assertThat(result.getSource()).isEqualTo("ALTERNATIVE_ME");
        assertThat(result.getMetricType()).isEqualTo(SentimentMetricType.FEAR_GREED_INDEX);
        assertThat(result.getValidationStatus()).isEqualTo(snapshot.validation().status());
        assertThat(result.getIndexValue()).isEqualByComparingTo("54.00000000");
    }

    private FearGreedRawSnapshot rawSnapshot() {
        return new FearGreedRawSnapshot(
                SentimentMetricType.FEAR_GREED_INDEX,
                Instant.parse("2026-03-10T00:00:00Z"),
                RawDataValidationResult.valid(),
                new BigDecimal("54.00000000"),
                "Neutral",
                3600L,
                "{\"value\":\"54\"}"
        );
    }
}

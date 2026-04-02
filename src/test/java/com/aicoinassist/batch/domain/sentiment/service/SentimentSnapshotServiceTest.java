package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.sentiment.dto.SentimentSnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRawRepository;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentSnapshotServiceTest {

    @Mock
    private SentimentSnapshotRawRepository sentimentSnapshotRawRepository;

    @Mock
    private SentimentSnapshotRepository sentimentSnapshotRepository;

    @Test
    void createFearGreedSnapshotBuildsProcessedSnapshotFromLatestValidRaw() {
        SentimentSnapshotService service = new SentimentSnapshotService(
                sentimentSnapshotRawRepository,
                sentimentSnapshotRepository
        );

        SentimentSnapshotRawEntity latestRaw = rawEntity(RawDataValidationStatus.VALID, null, "54.00000000", "Neutral");
        SentimentSnapshotEntity previousSnapshot = SentimentSnapshotEntity.builder()
                                                                         .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                                                         .snapshotTime(Instant.parse("2026-03-09T00:00:00Z"))
                                                                         .sourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                                                         .sourceDataVersion("old")
                                                                         .indexValue(new BigDecimal("40.00000000"))
                                                                         .classification("Fear")
                                                                         .timeUntilUpdateSeconds(3600L)
                                                                         .build();

        when(sentimentSnapshotRawRepository.findTopByMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX
        )).thenReturn(Optional.of(latestRaw));
        when(sentimentSnapshotRepository.findTopByMetricTypeAndSnapshotTimeLessThanOrderBySnapshotTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX,
                latestRaw.getSourceEventTime()
        )).thenReturn(Optional.of(previousSnapshot));

        SentimentSnapshot snapshot = service.createFearGreedSnapshot();

        assertThat(snapshot.metricType()).isEqualTo(SentimentMetricType.FEAR_GREED_INDEX);
        assertThat(snapshot.snapshotTime()).isEqualTo(latestRaw.getSourceEventTime());
        assertThat(snapshot.valueChange()).isEqualByComparingTo("14.00000000");
        assertThat(snapshot.valueChangeRate()).isEqualByComparingTo("0.35000000");
        assertThat(snapshot.classificationChanged()).isTrue();
    }

    @Test
    void createFearGreedSnapshotRejectsInvalidRawSnapshot() {
        SentimentSnapshotService service = new SentimentSnapshotService(
                sentimentSnapshotRawRepository,
                sentimentSnapshotRepository
        );

        when(sentimentSnapshotRawRepository.findTopByMetricTypeOrderBySourceEventTimeDescCollectedTimeDescIdDesc(
                SentimentMetricType.FEAR_GREED_INDEX
        )).thenReturn(Optional.of(rawEntity(
                RawDataValidationStatus.INVALID,
                "Fear & Greed value must be numeric.",
                null,
                null
        )));

        assertThatThrownBy(service::createFearGreedSnapshot)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fear & Greed raw snapshot is invalid");
    }

    private SentimentSnapshotRawEntity rawEntity(
            RawDataValidationStatus validationStatus,
            String validationDetails,
            String indexValue,
            String classification
    ) {
        return SentimentSnapshotRawEntity.builder()
                                         .source("ALTERNATIVE_ME")
                                         .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                         .sourceEventTime(Instant.parse("2026-03-10T00:00:00Z"))
                                         .collectedTime(Instant.parse("2026-03-10T00:01:00Z"))
                                         .validationStatus(validationStatus)
                                         .validationDetails(validationDetails)
                                         .indexValue(indexValue == null ? null : new BigDecimal(indexValue))
                                         .classification(classification)
                                         .timeUntilUpdateSeconds(3600L)
                                         .rawPayload("{\"value\":\"54\"}")
                                         .build();
    }
}

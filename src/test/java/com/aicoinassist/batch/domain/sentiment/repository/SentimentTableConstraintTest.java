package com.aicoinassist.batch.domain.sentiment.repository;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotRawEntity;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SentimentTableConstraintTest {

    @Autowired
    private SentimentSnapshotRawRepository sentimentSnapshotRawRepository;

    @Autowired
    private SentimentSnapshotRepository sentimentSnapshotRepository;

    @Test
    void sentimentSnapshotRawRejectsDuplicateSourceMetricAndSourceEventTime() {
        Instant sourceEventTime = Instant.parse("2026-03-10T00:00:00Z");

        sentimentSnapshotRawRepository.saveAndFlush(rawEntity(sourceEventTime, "54.00000000"));

        assertThatThrownBy(() -> sentimentSnapshotRawRepository.saveAndFlush(
                rawEntity(sourceEventTime, "55.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sentimentSnapshotRejectsDuplicateMetricAndSnapshotTime() {
        Instant snapshotTime = Instant.parse("2026-03-10T00:00:00Z");

        sentimentSnapshotRepository.saveAndFlush(snapshotEntity(snapshotTime, "54.00000000"));

        assertThatThrownBy(() -> sentimentSnapshotRepository.saveAndFlush(
                snapshotEntity(snapshotTime, "55.00000000")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private SentimentSnapshotRawEntity rawEntity(Instant sourceEventTime, String indexValue) {
        return SentimentSnapshotRawEntity.builder()
                                         .source("ALTERNATIVE_ME")
                                         .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                         .sourceEventTime(sourceEventTime)
                                         .collectedTime(sourceEventTime.plusSeconds(60))
                                         .validationStatus(RawDataValidationStatus.VALID)
                                         .indexValue(new BigDecimal(indexValue))
                                         .classification("Neutral")
                                         .timeUntilUpdateSeconds(3600L)
                                         .rawPayload("{\"value\":\"" + indexValue + "\"}")
                                         .build();
    }

    private SentimentSnapshotEntity snapshotEntity(Instant snapshotTime, String indexValue) {
        return SentimentSnapshotEntity.builder()
                                      .metricType(SentimentMetricType.FEAR_GREED_INDEX)
                                      .snapshotTime(snapshotTime)
                                      .sourceEventTime(snapshotTime)
                                      .sourceDataVersion("metricType=FEAR_GREED_INDEX;sourceEventTime=" + snapshotTime)
                                      .indexValue(new BigDecimal(indexValue))
                                      .classification("Neutral")
                                      .timeUntilUpdateSeconds(3600L)
                                      .previousSnapshotTime(snapshotTime.minusSeconds(86400))
                                      .previousIndexValue(new BigDecimal("40.00000000"))
                                      .valueChange(new BigDecimal("14.00000000"))
                                      .valueChangeRate(new BigDecimal("0.35000000"))
                                      .classificationChanged(true)
                                      .build();
    }
}

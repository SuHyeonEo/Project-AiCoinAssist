package com.aicoinassist.batch.domain.news.repository;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import com.aicoinassist.batch.domain.news.entity.NewsSignalSnapshotEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class NewsTableConstraintTest {

    @Autowired
    private NewsSignalRawRepository newsSignalRawRepository;

    @Autowired
    private NewsSignalSnapshotRepository newsSignalSnapshotRepository;

    @Test
    void newsSignalRawRejectsDuplicateSourceAssetAndArticleUrl() {
        newsSignalRawRepository.saveAndFlush(rawEntity("https://example.com/bitcoin"));

        assertThatThrownBy(() -> newsSignalRawRepository.saveAndFlush(
                rawEntity("https://example.com/bitcoin")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void newsSignalSnapshotRejectsDuplicateSymbolAndArticleUrl() {
        newsSignalSnapshotRepository.saveAndFlush(snapshotEntity("https://example.com/bitcoin"));

        assertThatThrownBy(() -> newsSignalSnapshotRepository.saveAndFlush(
                snapshotEntity("https://example.com/bitcoin")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private NewsSignalRawEntity rawEntity(String articleUrl) {
        return NewsSignalRawEntity.builder()
                                  .source("GDELT")
                                  .assetCode("btc")
                                  .queryText("bitcoin OR btc")
                                  .seenTime(Instant.parse("2026-03-10T12:00:00Z"))
                                  .articleUrl(articleUrl)
                                  .title("Bitcoin rebounds on ETF optimism")
                                  .domain("example.com")
                                  .sourceLanguage("English")
                                  .sourceCountry("US")
                                  .collectedTime(Instant.parse("2026-03-10T12:05:00Z"))
                                  .validationStatus(RawDataValidationStatus.VALID)
                                  .rawPayload("{\"title\":\"Bitcoin rebounds on ETF optimism\"}")
                                  .build();
    }

    private NewsSignalSnapshotEntity snapshotEntity(String articleUrl) {
        return NewsSignalSnapshotEntity.builder()
                                       .symbol("BTC")
                                       .assetCode("btc")
                                       .snapshotTime(Instant.parse("2026-03-10T12:00:00Z"))
                                       .seenTime(Instant.parse("2026-03-10T12:00:00Z"))
                                       .sourceDataVersion("seenTime=2026-03-10T12:00:00Z;urlHash=abc123")
                                       .articleUrl(articleUrl)
                                       .title("Bitcoin rebounds on ETF optimism")
                                       .domain("example.com")
                                       .sourceLanguage("English")
                                       .sourceCountry("US")
                                       .titleKeywordHitCount(1)
                                       .priorityScore(new BigDecimal("0.5000"))
                                       .build();
    }
}

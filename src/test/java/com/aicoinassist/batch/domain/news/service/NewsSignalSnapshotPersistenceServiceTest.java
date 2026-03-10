package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.news.dto.NewsSignalSnapshot;
import com.aicoinassist.batch.domain.news.entity.NewsSignalSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalSnapshotRepository;
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
class NewsSignalSnapshotPersistenceServiceTest {

    @Mock
    private NewsSignalSnapshotService newsSignalSnapshotService;

    @Mock
    private NewsSignalSnapshotRepository newsSignalSnapshotRepository;

    @Test
    void persistLatestSignalsRefreshesExistingSnapshotWhenArticleUrlMatches() {
        NewsSignalSnapshotPersistenceService service = new NewsSignalSnapshotPersistenceService(
                newsSignalSnapshotService,
                newsSignalSnapshotRepository
        );
        NewsSignalSnapshot snapshot = snapshot();
        NewsSignalSnapshotEntity existingEntity = NewsSignalSnapshotEntity.builder()
                                                                          .symbol("BTC")
                                                                          .assetCode("btc")
                                                                          .snapshotTime(snapshot.snapshotTime().minusSeconds(60))
                                                                          .seenTime(snapshot.seenTime().minusSeconds(60))
                                                                          .sourceDataVersion("old")
                                                                          .articleUrl(snapshot.articleUrl())
                                                                          .title("old title")
                                                                          .titleKeywordHitCount(1)
                                                                          .priorityScore(new BigDecimal("0.5000"))
                                                                          .build();

        when(newsSignalSnapshotService.createLatestSnapshots(AssetType.BTC)).thenReturn(List.of(snapshot));
        when(newsSignalSnapshotRepository.findTopBySymbolAndArticleUrlOrderBySnapshotTimeDescIdDesc(
                "BTC",
                snapshot.articleUrl()
        )).thenReturn(Optional.of(existingEntity));

        List<NewsSignalSnapshotEntity> result = service.persistLatestSignals(AssetType.BTC);

        verify(newsSignalSnapshotRepository, never()).save(any(NewsSignalSnapshotEntity.class));
        assertThat(result).hasSize(1);
        assertThat(existingEntity.getPriorityScore()).isEqualByComparingTo("0.7500");
        assertThat(existingEntity.getTitle()).isEqualTo(snapshot.title());
    }

    @Test
    void persistLatestSignalsSavesNewSnapshotWhenArticleUrlDoesNotExist() {
        NewsSignalSnapshotPersistenceService service = new NewsSignalSnapshotPersistenceService(
                newsSignalSnapshotService,
                newsSignalSnapshotRepository
        );
        NewsSignalSnapshot snapshot = snapshot();

        when(newsSignalSnapshotService.createLatestSnapshots(AssetType.BTC)).thenReturn(List.of(snapshot));
        when(newsSignalSnapshotRepository.findTopBySymbolAndArticleUrlOrderBySnapshotTimeDescIdDesc(
                "BTC",
                snapshot.articleUrl()
        )).thenReturn(Optional.empty());
        when(newsSignalSnapshotRepository.save(any(NewsSignalSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NewsSignalSnapshotEntity> result = service.persistLatestSignals(AssetType.BTC);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitleKeywordHitCount()).isEqualTo(2);
        assertThat(result.get(0).getPriorityScore()).isEqualByComparingTo("0.7500");
    }

    private NewsSignalSnapshot snapshot() {
        return new NewsSignalSnapshot(
                "BTC",
                "btc",
                Instant.parse("2026-03-10T12:00:00Z"),
                Instant.parse("2026-03-10T12:00:00Z"),
                "seenTime=2026-03-10T12:00:00Z;urlHash=abc123",
                "https://example.com/bitcoin",
                "Bitcoin and BTC rebound on ETF optimism",
                "example.com",
                "English",
                "US",
                2,
                new BigDecimal("0.7500")
        );
    }
}

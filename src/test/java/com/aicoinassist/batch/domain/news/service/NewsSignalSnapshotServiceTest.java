package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.news.dto.NewsSignalSnapshot;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalRawRepository;
import com.aicoinassist.batch.domain.news.support.NewsAssetKeywordSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsSignalSnapshotServiceTest {

    @Mock
    private NewsSignalRawRepository newsSignalRawRepository;

    private final NewsAssetKeywordSupport newsAssetKeywordSupport = new NewsAssetKeywordSupport();

    @Test
    void createLatestSnapshotsBuildsPriorityScoreAndSourceDataVersion() {
        NewsSignalSnapshotService service = new NewsSignalSnapshotService(newsSignalRawRepository, newsAssetKeywordSupport);

        when(newsSignalRawRepository.findTop20ByAssetCodeAndValidationStatusOrderBySeenTimeDescCollectedTimeDescIdDesc(
                "btc",
                RawDataValidationStatus.VALID
        )).thenReturn(List.of(rawEntity()));

        List<NewsSignalSnapshot> snapshots = service.createLatestSnapshots(AssetType.BTC);

        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).titleKeywordHitCount()).isEqualTo(2);
        assertThat(snapshots.get(0).priorityScore()).isEqualByComparingTo("0.7500");
        assertThat(snapshots.get(0).sourceDataVersion()).contains("seenTime=2026-03-10T12:00:00Z");
    }

    private NewsSignalRawEntity rawEntity() {
        return NewsSignalRawEntity.builder()
                                  .source("GDELT")
                                  .assetCode("btc")
                                  .queryText("bitcoin OR btc")
                                  .seenTime(Instant.parse("2026-03-10T12:00:00Z"))
                                  .articleUrl("https://example.com/bitcoin")
                                  .title("Bitcoin and BTC both rebound on ETF optimism")
                                  .domain("example.com")
                                  .sourceLanguage("English")
                                  .sourceCountry("US")
                                  .collectedTime(Instant.parse("2026-03-10T12:05:00Z"))
                                  .validationStatus(RawDataValidationStatus.VALID)
                                  .rawPayload("{\"title\":\"Bitcoin and BTC both rebound on ETF optimism\"}")
                                  .build();
    }
}

package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.news.dto.GdeltNewsRawSignal;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalRawRepository;
import com.aicoinassist.batch.infrastructure.client.gdelt.GdeltNewsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsRawIngestionServiceTest {

    @Mock
    private GdeltNewsClient gdeltNewsClient;

    @Mock
    private NewsSignalRawRepository newsSignalRawRepository;

    @Test
    void ingestAssetSignalsRefreshesExistingRawWhenArticleUrlMatches() {
        NewsRawIngestionService service = new NewsRawIngestionService(gdeltNewsClient, newsSignalRawRepository);
        GdeltNewsRawSignal rawSignal = rawSignal();
        NewsSignalRawEntity existingEntity = NewsSignalRawEntity.builder()
                                                                .source("GDELT")
                                                                .assetCode("btc")
                                                                .queryText("bitcoin OR btc")
                                                                .articleUrl(rawSignal.articleUrl())
                                                                .title("old title")
                                                                .domain("old.example.com")
                                                                .collectedTime(Instant.parse("2026-03-10T00:10:00Z"))
                                                                .validationStatus(rawSignal.validation().status())
                                                                .rawPayload("{\"old\":true}")
                                                                .build();

        when(gdeltNewsClient.fetchLatestSignals(AssetType.BTC)).thenReturn(List.of(rawSignal));
        when(newsSignalRawRepository.findTopBySourceAndAssetCodeAndArticleUrlOrderByCollectedTimeDescIdDesc(
                "GDELT",
                "btc",
                rawSignal.articleUrl()
        )).thenReturn(Optional.of(existingEntity));

        List<NewsSignalRawEntity> result = service.ingestAssetSignals(AssetType.BTC);

        verify(newsSignalRawRepository, never()).save(any(NewsSignalRawEntity.class));
        assertThat(result).hasSize(1);
        assertThat(existingEntity.getTitle()).isEqualTo("Bitcoin surges after macro risk eases");
        assertThat(existingEntity.getDomain()).isEqualTo("example.com");
    }

    @Test
    void ingestAssetSignalsPersistsNewRawWhenArticleUrlDoesNotExist() {
        NewsRawIngestionService service = new NewsRawIngestionService(gdeltNewsClient, newsSignalRawRepository);
        GdeltNewsRawSignal rawSignal = rawSignal();

        when(gdeltNewsClient.fetchLatestSignals(AssetType.BTC)).thenReturn(List.of(rawSignal));
        when(newsSignalRawRepository.findTopBySourceAndAssetCodeAndArticleUrlOrderByCollectedTimeDescIdDesc(
                "GDELT",
                "btc",
                rawSignal.articleUrl()
        )).thenReturn(Optional.empty());
        when(newsSignalRawRepository.save(any(NewsSignalRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<NewsSignalRawEntity> result = service.ingestAssetSignals(AssetType.BTC);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssetCode()).isEqualTo("btc");
        assertThat(result.get(0).getValidationStatus()).isEqualTo(rawSignal.validation().status());
        assertThat(result.get(0).getTitle()).isEqualTo(rawSignal.title());
    }

    private GdeltNewsRawSignal rawSignal() {
        return new GdeltNewsRawSignal(
                "btc",
                "bitcoin OR btc",
                Instant.parse("2026-03-10T12:00:00Z"),
                RawDataValidationResult.valid(),
                "https://example.com/bitcoin",
                "https://m.example.com/bitcoin",
                "Bitcoin surges after macro risk eases",
                "example.com",
                "English",
                "US",
                "https://example.com/image.jpg",
                "{\"title\":\"Bitcoin surges after macro risk eases\"}"
        );
    }
}

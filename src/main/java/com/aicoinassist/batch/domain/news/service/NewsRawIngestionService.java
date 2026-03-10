package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.news.dto.GdeltNewsRawSignal;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalRawRepository;
import com.aicoinassist.batch.infrastructure.client.gdelt.GdeltNewsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsRawIngestionService {

    private static final String GDELT_SOURCE = "GDELT";

    private final GdeltNewsClient gdeltNewsClient;
    private final NewsSignalRawRepository newsSignalRawRepository;

    @Transactional
    public List<NewsSignalRawEntity> ingestAssetSignals(AssetType assetType) {
        Instant collectedTime = Instant.now();
        List<GdeltNewsRawSignal> rawSignals = gdeltNewsClient.fetchLatestSignals(assetType);
        List<NewsSignalRawEntity> storedEntities = new ArrayList<>();

        for (GdeltNewsRawSignal rawSignal : rawSignals) {
            NewsSignalRawEntity existingEntity = rawSignal.articleUrl() == null || rawSignal.articleUrl().isBlank()
                    ? null
                    : newsSignalRawRepository
                            .findTopBySourceAndAssetCodeAndArticleUrlOrderByCollectedTimeDescIdDesc(
                                    GDELT_SOURCE,
                                    rawSignal.assetCode(),
                                    rawSignal.articleUrl()
                            )
                            .orElse(null);

            if (existingEntity == null) {
                NewsSignalRawEntity entity = NewsSignalRawEntity.builder()
                                                                .source(GDELT_SOURCE)
                                                                .assetCode(rawSignal.assetCode())
                                                                .queryText(rawSignal.queryText())
                                                                .seenTime(rawSignal.seenTime())
                                                                .articleUrl(rawSignal.articleUrl())
                                                                .mobileUrl(rawSignal.mobileUrl())
                                                                .title(rawSignal.title())
                                                                .domain(rawSignal.domain())
                                                                .sourceLanguage(rawSignal.sourceLanguage())
                                                                .sourceCountry(rawSignal.sourceCountry())
                                                                .socialImageUrl(rawSignal.socialImageUrl())
                                                                .collectedTime(collectedTime)
                                                                .validationStatus(rawSignal.validation().status())
                                                                .validationDetails(rawSignal.validation().details())
                                                                .rawPayload(rawSignal.rawPayload())
                                                                .build();
                storedEntities.add(newsSignalRawRepository.save(entity));
                continue;
            }

            existingEntity.refreshFromIngestion(
                    rawSignal.seenTime(),
                    rawSignal.mobileUrl(),
                    rawSignal.title(),
                    rawSignal.domain(),
                    rawSignal.sourceLanguage(),
                    rawSignal.sourceCountry(),
                    rawSignal.socialImageUrl(),
                    collectedTime,
                    rawSignal.validation().status(),
                    rawSignal.validation().details(),
                    rawSignal.rawPayload()
            );
            storedEntities.add(existingEntity);
        }

        return storedEntities;
    }

    @Transactional
    public void ingestAllAssets() {
        ingestAssetSignals(AssetType.BTC);
        ingestAssetSignals(AssetType.ETH);
        ingestAssetSignals(AssetType.XRP);
    }
}

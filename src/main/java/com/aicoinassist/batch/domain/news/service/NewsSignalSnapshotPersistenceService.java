package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.news.dto.NewsSignalSnapshot;
import com.aicoinassist.batch.domain.news.entity.NewsSignalSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.NewsSignalSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsSignalSnapshotPersistenceService {

    private final NewsSignalSnapshotService newsSignalSnapshotService;
    private final NewsSignalSnapshotRepository newsSignalSnapshotRepository;

    @Transactional
    public List<NewsSignalSnapshotEntity> persistLatestSignals(AssetType assetType) {
        List<NewsSignalSnapshot> snapshots = newsSignalSnapshotService.createLatestSnapshots(assetType);
        List<NewsSignalSnapshotEntity> storedEntities = new ArrayList<>();

        for (NewsSignalSnapshot snapshot : snapshots) {
            NewsSignalSnapshotEntity existingEntity = newsSignalSnapshotRepository
                    .findTopBySymbolAndArticleUrlOrderBySnapshotTimeDescIdDesc(snapshot.symbol(), snapshot.articleUrl())
                    .orElse(null);

            if (existingEntity == null) {
                NewsSignalSnapshotEntity entity = NewsSignalSnapshotEntity.builder()
                                                                         .symbol(snapshot.symbol())
                                                                         .assetCode(snapshot.assetCode())
                                                                         .snapshotTime(snapshot.snapshotTime())
                                                                         .seenTime(snapshot.seenTime())
                                                                         .sourceDataVersion(snapshot.sourceDataVersion())
                                                                         .articleUrl(snapshot.articleUrl())
                                                                         .title(snapshot.title())
                                                                         .domain(snapshot.domain())
                                                                         .sourceLanguage(snapshot.sourceLanguage())
                                                                         .sourceCountry(snapshot.sourceCountry())
                                                                         .titleKeywordHitCount(snapshot.titleKeywordHitCount())
                                                                         .priorityScore(snapshot.priorityScore())
                                                                         .build();
                storedEntities.add(newsSignalSnapshotRepository.save(entity));
                continue;
            }

            existingEntity.refreshFromSnapshot(
                    snapshot.snapshotTime(),
                    snapshot.seenTime(),
                    snapshot.sourceDataVersion(),
                    snapshot.title(),
                    snapshot.domain(),
                    snapshot.sourceLanguage(),
                    snapshot.sourceCountry(),
                    snapshot.titleKeywordHitCount(),
                    snapshot.priorityScore()
            );
            storedEntities.add(existingEntity);
        }

        return storedEntities;
    }
}

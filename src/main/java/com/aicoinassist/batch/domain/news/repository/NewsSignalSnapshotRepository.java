package com.aicoinassist.batch.domain.news.repository;

import com.aicoinassist.batch.domain.news.entity.NewsSignalSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsSignalSnapshotRepository extends JpaRepository<NewsSignalSnapshotEntity, Long> {

    Optional<NewsSignalSnapshotEntity> findTopBySymbolAndArticleUrlOrderBySnapshotTimeDescIdDesc(
            String symbol,
            String articleUrl
    );
}

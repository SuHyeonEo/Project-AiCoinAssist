package com.aicoinassist.batch.domain.news.repository;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.news.entity.NewsSignalRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsSignalRawRepository extends JpaRepository<NewsSignalRawEntity, Long> {

    Optional<NewsSignalRawEntity> findTopBySourceAndAssetCodeAndArticleUrlOrderByCollectedTimeDescIdDesc(
            String source,
            String assetCode,
            String articleUrl
    );

    List<NewsSignalRawEntity> findTop20ByAssetCodeAndValidationStatusOrderBySeenTimeDescCollectedTimeDescIdDesc(
            String assetCode,
            RawDataValidationStatus validationStatus
    );
}

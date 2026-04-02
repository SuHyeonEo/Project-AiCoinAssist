package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGenerationResult;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotDraft;
import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;
import com.aicoinassist.batch.domain.news.repository.ReferenceNewsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ReferenceNewsSnapshotGenerationFlowService {

    private final ReferenceNewsProperties referenceNewsProperties;
    private final ReferenceNewsSnapshotRepository referenceNewsSnapshotRepository;
    private final ReferenceNewsGenerationService referenceNewsGenerationService;
    private final ReferenceNewsSnapshotDraftFactory referenceNewsSnapshotDraftFactory;
    private final ReferenceNewsSnapshotPersistenceService referenceNewsSnapshotPersistenceService;
    private final Clock clock;

    @Transactional
    public ReferenceNewsSnapshotGenerationFlowResult generateTodayIfMissing() {
        return generateForDateIfMissing(LocalDate.now(clock.withZone(ZoneOffset.UTC)));
    }

    @Transactional
    public ReferenceNewsSnapshotGenerationFlowResult generateForDateIfMissing(LocalDate snapshotDate) {
        return referenceNewsSnapshotRepository
                .findTopByScopeAndSnapshotDateOrderByIdDesc(referenceNewsProperties.scope(), snapshotDate)
                .map(existing -> new ReferenceNewsSnapshotGenerationFlowResult(false, existing))
                .orElseGet(() -> generateAndStore(snapshotDate));
    }

    private ReferenceNewsSnapshotGenerationFlowResult generateAndStore(LocalDate snapshotDate) {
        Instant requestedAt = clock.instant();
        ReferenceNewsGenerationResult generationResult = referenceNewsGenerationService.generate(snapshotDate);
        Instant completedAt = clock.instant();
        Instant storedAt = clock.instant();

        ReferenceNewsSnapshotDraft draft = referenceNewsSnapshotDraftFactory.create(
                snapshotDate,
                generationResult,
                requestedAt,
                completedAt,
                storedAt
        );
        ReferenceNewsSnapshotEntity snapshot = referenceNewsSnapshotPersistenceService.save(draft);
        return new ReferenceNewsSnapshotGenerationFlowResult(true, snapshot);
    }
}

package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.entity.ReferenceNewsSnapshotEntity;

public record ReferenceNewsSnapshotGenerationFlowResult(
        boolean created,
        ReferenceNewsSnapshotEntity snapshot
) {
}

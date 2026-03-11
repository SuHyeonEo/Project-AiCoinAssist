package com.aicoinassist.batch.domain.news.dto;

import java.util.List;

public record ReferenceNewsSnapshotPayload(
        String summary,
        List<ReferenceNewsItem> items
) {
}

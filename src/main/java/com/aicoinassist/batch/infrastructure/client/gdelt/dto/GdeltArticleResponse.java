package com.aicoinassist.batch.infrastructure.client.gdelt.dto;

import java.util.List;

public record GdeltArticleResponse(
        List<GdeltArticleItem> articles
) {
}

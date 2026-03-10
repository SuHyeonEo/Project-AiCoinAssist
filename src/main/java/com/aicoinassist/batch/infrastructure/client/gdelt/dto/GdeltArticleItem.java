package com.aicoinassist.batch.infrastructure.client.gdelt.dto;

public record GdeltArticleItem(
        String url,
        String urlmobile,
        String title,
        String seendate,
        String socialimage,
        String domain,
        String language,
        String sourcecountry
) {
}

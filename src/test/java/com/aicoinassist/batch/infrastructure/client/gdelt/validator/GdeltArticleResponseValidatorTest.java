package com.aicoinassist.batch.infrastructure.client.gdelt.validator;

import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleItem;
import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GdeltArticleResponseValidatorTest {

    private final GdeltArticleResponseValidator validator = new GdeltArticleResponseValidator();

    @Test
    void validateResponseReturnsInvalidWhenArticlesMissing() {
        assertThat(validator.validateResponse(new GdeltArticleResponse(List.of())).isValid()).isFalse();
    }

    @Test
    void validateArticleReturnsValidForUsableArticle() {
        GdeltArticleItem item = new GdeltArticleItem(
                "https://example.com/bitcoin",
                "https://m.example.com/bitcoin",
                "Bitcoin rallies after ETF inflow surprise",
                "20260310T120000Z",
                "https://example.com/image.jpg",
                "example.com",
                "English",
                "US"
        );

        assertThat(validator.validateArticle(item).isValid()).isTrue();
    }

    @Test
    void validateArticleReturnsInvalidWhenSeenDateBroken() {
        GdeltArticleItem item = new GdeltArticleItem(
                "https://example.com/bitcoin",
                null,
                "Bitcoin rallies after ETF inflow surprise",
                "2026-03-10",
                null,
                "example.com",
                "English",
                "US"
        );

        assertThat(validator.validateArticle(item).details())
                .isEqualTo("GDELT article seendate must follow yyyyMMddTHHmmssZ.");
    }
}

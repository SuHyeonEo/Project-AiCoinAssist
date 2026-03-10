package com.aicoinassist.batch.infrastructure.client.gdelt;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.news.dto.GdeltNewsRawSignal;
import com.aicoinassist.batch.domain.news.support.NewsAssetKeywordSupport;
import com.aicoinassist.batch.global.config.GdeltProperties;
import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleItem;
import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleResponse;
import com.aicoinassist.batch.infrastructure.client.gdelt.validator.GdeltArticleResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GdeltNewsClient {

    private static final DateTimeFormatter GDELT_SEEN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GdeltProperties gdeltProperties;
    private final GdeltArticleResponseValidator validator;
    private final NewsAssetKeywordSupport newsAssetKeywordSupport;

    public List<GdeltNewsRawSignal> fetchLatestSignals(AssetType assetType) {
        String queryText = newsAssetKeywordSupport.queryText(assetType);
        String assetCode = newsAssetKeywordSupport.assetCode(assetType);
        String rawPayload = restClient.get()
                                      .uri(requestUrl(queryText))
                                      .retrieve()
                                      .body(String.class);

        if (rawPayload == null) {
            throw new IllegalStateException("GDELT API returned an empty payload for " + assetType + ".");
        }

        GdeltArticleResponse response = deserialize(rawPayload);
        RawDataValidationResult responseValidation = validator.validateResponse(response);
        if (!responseValidation.isValid()) {
            return List.of(invalidPlaceholder(assetCode, queryText, responseValidation, rawPayload));
        }

        return response.articles().stream()
                       .map(article -> toRawSignal(assetCode, queryText, article))
                       .toList();
    }

    private String requestUrl(String queryText) {
        return gdeltProperties.baseUrl()
                + "?query=" + encodeQueryText(queryText)
                + "&mode=artlist"
                + "&maxrecords=" + gdeltProperties.maxRecords()
                + "&format=json"
                + "&sort=datedesc"
                + "&timespan=" + gdeltProperties.timeSpan();
    }

    private String encodeQueryText(String queryText) {
        try {
            return URLEncoder.encode(queryText, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 encoding must be available.", exception);
        }
    }

    private GdeltArticleResponse deserialize(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, GdeltArticleResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize GDELT article response.", exception);
        }
    }

    private GdeltNewsRawSignal invalidPlaceholder(
            String assetCode,
            String queryText,
            RawDataValidationResult validation,
            String rawPayload
    ) {
        return new GdeltNewsRawSignal(
                assetCode,
                queryText,
                null,
                validation,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                rawPayload
        );
    }

    private GdeltNewsRawSignal toRawSignal(String assetCode, String queryText, GdeltArticleItem article) {
        RawDataValidationResult itemValidation = validator.validateArticle(article);
        return new GdeltNewsRawSignal(
                assetCode,
                queryText,
                parseSeenTime(article == null ? null : article.seendate()),
                itemValidation,
                article == null ? null : article.url(),
                article == null ? null : article.urlmobile(),
                article == null ? null : article.title(),
                article == null ? null : article.domain(),
                article == null ? null : article.language(),
                article == null ? null : article.sourcecountry(),
                article == null ? null : article.socialimage(),
                serializeArticle(article)
        );
    }

    private Instant parseSeenTime(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank()
                    ? null
                    : Instant.from(GDELT_SEEN_DATE_FORMATTER.parse(rawValue));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String serializeArticle(GdeltArticleItem article) {
        try {
            return objectMapper.writeValueAsString(article);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize GDELT article item.", exception);
        }
    }
}

package com.aicoinassist.batch.infrastructure.client.gdelt.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleItem;
import com.aicoinassist.batch.infrastructure.client.gdelt.dto.GdeltArticleResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class GdeltArticleResponseValidator {

    private static final DateTimeFormatter GDELT_SEEN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    public RawDataValidationResult validateResponse(GdeltArticleResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("GDELT response must not be null.");
        }

        if (response.articles() == null || response.articles().isEmpty()) {
            return RawDataValidationResult.invalid("GDELT response must contain at least one article.");
        }

        return RawDataValidationResult.valid();
    }

    public RawDataValidationResult validateArticle(GdeltArticleItem item) {
        if (item == null) {
            return RawDataValidationResult.invalid("GDELT article item must not be null.");
        }

        if (item.url() == null || item.url().isBlank()) {
            return RawDataValidationResult.invalid("GDELT article url must not be blank.");
        }

        try {
            URI.create(item.url());
        } catch (RuntimeException exception) {
            return RawDataValidationResult.invalid("GDELT article url must be a valid URI.");
        }

        if (item.title() == null || item.title().isBlank()) {
            return RawDataValidationResult.invalid("GDELT article title must not be blank.");
        }

        if (item.seendate() == null || item.seendate().isBlank()) {
            return RawDataValidationResult.invalid("GDELT article seendate must not be blank.");
        }

        try {
            Instant.from(GDELT_SEEN_DATE_FORMATTER.parse(item.seendate()));
        } catch (DateTimeParseException exception) {
            return RawDataValidationResult.invalid("GDELT article seendate must follow yyyyMMddTHHmmssZ.");
        }

        if (item.domain() == null || item.domain().isBlank()) {
            return RawDataValidationResult.invalid("GDELT article domain must not be blank.");
        }

        return RawDataValidationResult.valid();
    }
}

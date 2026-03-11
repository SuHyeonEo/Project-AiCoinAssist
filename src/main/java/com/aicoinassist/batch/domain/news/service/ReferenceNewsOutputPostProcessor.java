package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsCategory;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsItem;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsOutputLengthPolicy;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import com.aicoinassist.batch.domain.news.enumtype.ReferenceNewsGenerationFailureType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReferenceNewsOutputPostProcessor {

    private final ObjectMapper objectMapper;

    public ReferenceNewsSnapshotPayload process(String rawOutputJson) {
        ReferenceNewsSnapshotPayload payload = deserialize(rawOutputJson);
        validate(payload, ReferenceNewsOutputLengthPolicy.defaults());
        return payload;
    }

    private ReferenceNewsSnapshotPayload deserialize(String rawOutputJson) {
        try {
            return objectMapper.readValue(rawOutputJson, ReferenceNewsSnapshotPayload.class);
        } catch (JsonProcessingException exception) {
            throw new ReferenceNewsGatewayException(
                    ReferenceNewsGenerationFailureType.CONTENT,
                    false,
                    "Failed to deserialize reference news output JSON.",
                    exception
            );
        }
    }

    private void validate(ReferenceNewsSnapshotPayload payload, ReferenceNewsOutputLengthPolicy policy) {
        if (payload == null) {
            throw contentError("Reference news payload is missing.");
        }
        if (payload.summary() == null || payload.summary().isBlank()) {
            throw contentError("Reference news summary is missing.");
        }
        if (payload.summary().length() > policy.summaryMaxChars()) {
            throw contentError("Reference news summary exceeded the allowed length.");
        }
        if (payload.items() == null || payload.items().size() != policy.maxItems()) {
            throw contentError("Reference news item count must be exactly " + policy.maxItems() + ".");
        }

        boolean hasDirectAsset = false;
        boolean hasMacroEconomy = false;
        Set<String> normalizedUrls = new HashSet<>();
        for (ReferenceNewsItem item : payload.items()) {
            validateItem(item, policy, normalizedUrls);
            hasDirectAsset |= item.category() == ReferenceNewsCategory.DIRECT_ASSET;
            hasMacroEconomy |= item.category() == ReferenceNewsCategory.MACRO_ECONOMY;
        }

        if (!hasDirectAsset) {
            throw contentError("Reference news output must contain at least one DIRECT_ASSET item.");
        }
        if (!hasMacroEconomy) {
            throw contentError("Reference news output must contain at least one MACRO_ECONOMY item.");
        }
    }

    private void validateItem(
            ReferenceNewsItem item,
            ReferenceNewsOutputLengthPolicy policy,
            Set<String> normalizedUrls
    ) {
        if (item == null) {
            throw contentError("Reference news item is missing.");
        }
        if (item.category() == null) {
            throw contentError("Reference news item category is missing.");
        }
        if (item.title() == null || item.title().isBlank() || item.title().length() > policy.titleMaxChars()) {
            throw contentError("Reference news item title is invalid.");
        }
        if (item.source() == null || item.source().isBlank() || item.source().length() > policy.sourceMaxChars()) {
            throw contentError("Reference news item source is invalid.");
        }
        if (item.publishedAt() == null) {
            throw contentError("Reference news item publishedAt is missing.");
        }
        if (item.url() == null || item.url().isBlank() || !(item.url().startsWith("http://") || item.url().startsWith("https://"))) {
            throw contentError("Reference news item URL is invalid.");
        }
        if (!normalizedUrls.add(item.url().trim().toLowerCase())) {
            throw contentError("Reference news output contains duplicate URLs.");
        }
        if (item.selectionReason() == null
                || item.selectionReason().isBlank()
                || item.selectionReason().length() > policy.selectionReasonMaxChars()) {
            throw contentError("Reference news item selectionReason is invalid.");
        }
    }

    private ReferenceNewsGatewayException contentError(String message) {
        return new ReferenceNewsGatewayException(
                ReferenceNewsGenerationFailureType.CONTENT,
                false,
                message
        );
    }
}

package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsCategory;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsOutputLengthPolicy;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsPromptComposition;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsPromptInputPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReferenceNewsPromptComposer {

    private final ReferenceNewsProperties referenceNewsProperties;
    private final ObjectMapper objectMapper;

    public ReferenceNewsPromptComposition compose(LocalDate snapshotDate) {
        ReferenceNewsOutputLengthPolicy outputLengthPolicy = ReferenceNewsOutputLengthPolicy.defaults();
        ReferenceNewsPromptInputPayload inputPayload = new ReferenceNewsPromptInputPayload(
                referenceNewsProperties.scope(),
                snapshotDate,
                outputLengthPolicy.maxItems(),
                List.of(ReferenceNewsCategory.DIRECT_ASSET, ReferenceNewsCategory.MACRO_ECONOMY),
                "Select exactly five items. Prefer the most relevant and recent items for crypto market interpretation. Avoid duplicates, rumors, promotional content, and low-signal headlines."
        );
        return new ReferenceNewsPromptComposition(
                systemPrompt(outputLengthPolicy),
                userPrompt(),
                serialize(inputPayload, "Failed to serialize reference news prompt input payload."),
                outputSchemaJson(),
                serialize(outputLengthPolicy, "Failed to serialize reference news output length policy.")
        );
    }

    private String systemPrompt(ReferenceNewsOutputLengthPolicy outputLengthPolicy) {
        return """
                You are selecting shared daily reference news for crypto market reports.

                Goal:
                - return exactly %d news items for the given snapshot date
                - cover at least one DIRECT_ASSET item and at least one MACRO_ECONOMY item
                - remaining items may be OTHER_REFERENCE when they materially help crypto interpretation

                Rules:
                - selection_reason must be short Korean prose
                - prefer high-signal developments that matter to BTC and broad crypto risk sentiment
                - avoid duplicates and near-duplicate angles about the same event
                - avoid rumors, opinion-only posts, advertisements, and weak market noise
                - use published_at as ISO-8601 UTC
                - use the source's canonical article URL when possible
                - summary must be concise Korean prose
                - do not invent unavailable facts
                """.formatted(outputLengthPolicy.maxItems());
    }

    private String userPrompt() {
        return """
                Use the input payload as the selection contract.

                Return JSON only.

                Required output shape:
                {
                  "summary": "string",
                  "items": [
                    {
                      "category": "DIRECT_ASSET | MACRO_ECONOMY | OTHER_REFERENCE",
                      "title": "string",
                      "source": "string",
                      "published_at": "ISO-8601 UTC instant",
                      "url": "https://...",
                      "selection_reason": "short Korean sentence"
                    }
                  ]
                }
                """;
    }

    private String outputSchemaJson() {
        return """
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["summary", "items"],
                  "properties": {
                    "summary": {
                      "type": "string"
                    },
                    "items": {
                      "type": "array",
                      "minItems": 5,
                      "maxItems": 5,
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["category", "title", "source", "published_at", "url", "selection_reason"],
                        "properties": {
                          "category": {
                            "type": "string",
                            "enum": ["DIRECT_ASSET", "MACRO_ECONOMY", "OTHER_REFERENCE"]
                          },
                          "title": {
                            "type": "string"
                          },
                          "source": {
                            "type": "string"
                          },
                          "published_at": {
                            "type": "string"
                          },
                          "url": {
                            "type": "string"
                          },
                          "selection_reason": {
                            "type": "string"
                          }
                        }
                      }
                    }
                  }
                }
                """;
    }

    private String serialize(Object value, String message) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(message, exception);
        }
    }
}

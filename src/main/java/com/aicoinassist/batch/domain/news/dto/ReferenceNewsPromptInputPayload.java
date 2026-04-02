package com.aicoinassist.batch.domain.news.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReferenceNewsPromptInputPayload(
        String scope,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDate snapshotDate,
        int targetItemCount,
        List<ReferenceNewsCategory> requiredCategories,
        String selectionGuidance
) {
}

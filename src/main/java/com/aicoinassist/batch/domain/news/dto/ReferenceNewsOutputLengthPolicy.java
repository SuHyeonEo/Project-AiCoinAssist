package com.aicoinassist.batch.domain.news.dto;

public record ReferenceNewsOutputLengthPolicy(
        int maxItems,
        int summaryMaxChars,
        int titleMaxChars,
        int sourceMaxChars,
        int selectionReasonMaxChars
) {

    public ReferenceNewsOutputLengthPolicy {
        maxItems = maxItems <= 0 ? 5 : maxItems;
        summaryMaxChars = summaryMaxChars <= 0 ? 140 : summaryMaxChars;
        titleMaxChars = titleMaxChars <= 0 ? 120 : titleMaxChars;
        sourceMaxChars = sourceMaxChars <= 0 ? 40 : sourceMaxChars;
        selectionReasonMaxChars = selectionReasonMaxChars <= 0 ? 80 : selectionReasonMaxChars;
    }

    public static ReferenceNewsOutputLengthPolicy defaults() {
        return new ReferenceNewsOutputLengthPolicy(5, 140, 120, 40, 80);
    }
}

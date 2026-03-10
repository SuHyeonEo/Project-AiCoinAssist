package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmOutputProcessingResult(
        AnalysisLlmNarrativeOutputPayload output,
        boolean fallbackUsed,
        List<String> issues
) {
}

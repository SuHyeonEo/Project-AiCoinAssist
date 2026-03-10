package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmDomainAnalysisOutput(
        String domain,
        String currentSignal,
        List<String> keyFacts,
        String interpretation,
        String pressure,
        String confidence,
        List<String> caveats
) {
}

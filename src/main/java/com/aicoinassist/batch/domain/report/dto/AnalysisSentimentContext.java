package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AnalysisSentimentContext(
        Instant snapshotTime,
        Instant sourceEventTime,
        String sourceDataVersion,
        BigDecimal indexValue,
        String classification,
        Long timeUntilUpdateSeconds,
        List<AnalysisSentimentComparisonFact> comparisonFacts,
        List<AnalysisSentimentWindowSummary> windowSummaries,
        List<AnalysisSentimentHighlight> highlights
) {
}

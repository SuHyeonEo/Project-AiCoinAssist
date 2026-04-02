package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmDomainFactBlock(
        AnalysisLlmDomainType domainType,
        String headline,
        String summary,
        List<String> keyFacts
) {
}

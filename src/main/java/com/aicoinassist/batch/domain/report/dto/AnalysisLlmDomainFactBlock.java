package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;

import java.util.List;

public record AnalysisLlmDomainFactBlock(
        AnalysisLlmDomainType domainType,
        String headline,
        String summary,
        List<String> keyFacts
) {
}

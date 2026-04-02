package com.aicoinassist.api.domain.report.dto;

public record SharedContextSummaryResponse(
	String contextVersion,
	String sharedSummary,
	DomainAnalysisResponse macro,
	DomainAnalysisResponse sentiment
) {
}

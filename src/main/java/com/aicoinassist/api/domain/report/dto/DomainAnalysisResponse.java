package com.aicoinassist.api.domain.report.dto;

public record DomainAnalysisResponse(
	String domain,
	String status,
	String interpretation,
	String watchPoint
) {
}

package com.aicoinassist.api.domain.report.dto;

public record ReportSignalHeadlineResponse(
	String category,
	String title,
	String detail,
	String importance
) {
}

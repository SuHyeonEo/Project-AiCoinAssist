package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record ReportHeaderResponse(
	String headline,
	String outlook,
	String confidence,
	String primaryMessage,
	String continuityMessage,
	String overallTone,
	String narrativeSummary,
	List<ReportSignalHeadlineResponse> signalHeadlines
) {
}

package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record ScenarioViewResponse(
	String scenarioType,
	String title,
	String condition,
	String trigger,
	String confirmation,
	String invalidation,
	String interpretation
) {
}

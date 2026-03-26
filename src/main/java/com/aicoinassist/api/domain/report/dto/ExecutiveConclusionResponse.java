package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record ExecutiveConclusionResponse(
	String summary,
	List<String> bullishFactors,
	List<String> bearishFactors,
	String tacticalView
) {
}

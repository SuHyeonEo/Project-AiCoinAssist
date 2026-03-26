package com.aicoinassist.api.domain.report.dto;

public record ExternalRegimeSignalResponse(
	String category,
	String title,
	String detail,
	String direction,
	String severity,
	String basisLabel
) {
}

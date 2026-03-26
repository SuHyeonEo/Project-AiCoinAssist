package com.aicoinassist.api.domain.report.dto;

public record ValueLabelBasisResponse(
	String value,
	String label,
	String basis,
	String windowType
) {
}

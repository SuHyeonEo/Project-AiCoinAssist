package com.aicoinassist.api.domain.report.dto;

public record MarketStructureBoxResponse(
	String rangeLow,
	String currentPrice,
	String rangeHigh,
	ValueLabelBasisResponse rangePosition,
	ValueLabelBasisResponse upsideReference,
	ValueLabelBasisResponse downsideReference,
	ValueLabelBasisResponse supportBreakRisk,
	ValueLabelBasisResponse resistanceBreakRisk,
	String interpretation
) {
}

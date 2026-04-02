package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmMarketStructureBoxOutput(
        String rangeLow,
        String currentPrice,
        String rangeHigh,
        AnalysisLlmValueLabelBasisOutput rangePosition,
        AnalysisLlmValueLabelBasisOutput upsideReference,
        AnalysisLlmValueLabelBasisOutput downsideReference,
        AnalysisLlmValueLabelBasisOutput supportBreakRisk,
        AnalysisLlmValueLabelBasisOutput resistanceBreakRisk,
        String interpretation
) {
}

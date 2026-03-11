package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmServerMarketStructureInput(
        String rangeLow,
        String currentPrice,
        String rangeHigh,
        AnalysisLlmValueLabelBasisOutput rangePosition,
        AnalysisLlmValueLabelBasisOutput upsideReference,
        AnalysisLlmValueLabelBasisOutput downsideReference,
        AnalysisLlmValueLabelBasisOutput supportBreakRisk,
        AnalysisLlmValueLabelBasisOutput resistanceBreakRisk
) {
}

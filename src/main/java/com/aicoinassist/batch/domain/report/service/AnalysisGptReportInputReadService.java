package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisGptReportInputReadService {

    private final AnalysisReportRepository analysisReportRepository;
    private final ObjectMapper objectMapper;
    private final AnalysisGptReportInputAssembler analysisGptReportInputAssembler;

    public AnalysisGptReportInputPayload getLatestInput(String symbol, AnalysisReportType reportType) {
        AnalysisReportEntity entity = analysisReportRepository
                .findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(symbol, reportType)
                .orElseThrow(() -> new IllegalArgumentException("Analysis report not found: " + symbol + ", " + reportType));
        return toInput(entity);
    }

    public AnalysisGptReportInputPayload toInput(AnalysisReportEntity entity) {
        return analysisGptReportInputAssembler.assemble(entity, deserialize(entity.getReportPayload()));
    }

    private AnalysisReportPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, AnalysisReportPayload.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize analysis report payload for GPT input.", exception);
        }
    }
}

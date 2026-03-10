package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.controller.AnalysisReportNarrativeNotFoundException;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeView;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisReportNarrativeReadService {

    private final AnalysisReportNarrativeRepository analysisReportNarrativeRepository;
    private final ObjectMapper objectMapper;

    public AnalysisReportNarrativeView getLatest(String symbol, AnalysisReportType reportType) {
        return analysisReportNarrativeRepository
                .findTopBySymbolAndReportTypeOrderByStoredAtDescIdDesc(symbol, reportType)
                .map(this::toView)
                .orElseThrow(() -> new AnalysisReportNarrativeNotFoundException(
                        "Report narrative not found: " + symbol + ", " + reportType
                ));
    }

    public AnalysisReportNarrativeView getById(Long id) {
        return analysisReportNarrativeRepository.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new AnalysisReportNarrativeNotFoundException(
                        "Report narrative not found: " + id
                ));
    }

    private AnalysisReportNarrativeView toView(AnalysisReportNarrativeEntity entity) {
        return new AnalysisReportNarrativeView(
                entity.getId(),
                entity.getAnalysisReport().getId(),
                entity.getSymbol(),
                entity.getReportType(),
                entity.getAnalysisBasisTime(),
                entity.getSourceDataVersion(),
                entity.getAnalysisEngineVersion(),
                entity.getLlmProvider(),
                entity.getLlmModel(),
                entity.getPromptTemplateVersion(),
                entity.getInputSchemaVersion(),
                entity.getOutputSchemaVersion(),
                entity.isFallbackUsed(),
                entity.getGenerationStatus(),
                entity.getFailureType(),
                entity.getProviderRequestId(),
                entity.getInputTokens(),
                entity.getOutputTokens(),
                entity.getTotalTokens(),
                entity.getRequestedAt(),
                entity.getCompletedAt(),
                entity.getStoredAt(),
                readJson(entity.getInputPayloadJson(), "input payload"),
                entity.getPromptSystemText(),
                entity.getPromptUserText(),
                readJson(entity.getOutputLengthPolicyJson(), "output length policy"),
                readJson(entity.getReferenceNewsJson(), "reference news"),
                entity.getRawOutputText(),
                readJson(entity.getOutputJson(), "output"),
                readJson(entity.getValidationIssuesJson(), "validation issues")
        );
    }

    private JsonNode readJson(String payload, String fieldName) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize " + fieldName + ".", exception);
        }
    }
}

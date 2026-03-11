package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportNarrativeDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalysisReportNarrativeGenerationFlowService {

    private final AnalysisReportRepository analysisReportRepository;
    private final AnalysisLlmNarrativeGenerationService analysisLlmNarrativeGenerationService;
    private final AnalysisReportNarrativeDraftFactory analysisReportNarrativeDraftFactory;
    private final AnalysisReportNarrativePersistenceService analysisReportNarrativePersistenceService;
    private final AnalysisLlmNarrativeProperties analysisLlmNarrativeProperties;
    private final OpenAiProperties openAiProperties;
    private final Clock clock;

    @Transactional
    public AnalysisReportNarrativeEntity generateAndStoreLatest(String symbol, AnalysisReportType reportType) {
        AnalysisReportEntity analysisReport = analysisReportRepository
                .findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(symbol, reportType)
                .orElseThrow(() -> new IllegalArgumentException("Analysis report not found: " + symbol + ", " + reportType));

        Instant requestedAt = clock.instant();
        AnalysisLlmNarrativeGenerationResult generationResult =
                analysisLlmNarrativeGenerationService.generateLatest(symbol, reportType);
        Instant completedAt = clock.instant();
        Instant storedAt = clock.instant();

        AnalysisReportNarrativeDraft draft = analysisReportNarrativeDraftFactory.create(
                analysisReport,
                generationResult,
                analysisLlmNarrativeProperties.provider(),
                openAiProperties.model(),
                analysisLlmNarrativeProperties.promptTemplateVersion(),
                analysisLlmNarrativeProperties.inputSchemaVersion(),
                analysisLlmNarrativeProperties.outputSchemaVersion(),
                requestedAt,
                completedAt,
                storedAt
        );

        return analysisReportNarrativePersistenceService.save(draft);
    }
}

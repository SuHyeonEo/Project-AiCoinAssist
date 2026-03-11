package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisLlmPromptCompositionReadService {

    private final AnalysisLlmNarrativeInputReadService analysisLlmNarrativeInputReadService;
    private final AnalysisLlmPromptComposer analysisLlmPromptComposer;

    public AnalysisLlmPromptComposition getLatestComposition(String symbol, AnalysisReportType reportType) {
        return analysisLlmPromptComposer.compose(
                analysisLlmNarrativeInputReadService.getLatestInput(symbol, reportType)
        );
    }
}

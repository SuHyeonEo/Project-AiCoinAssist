package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisLlmNarrativeInputReadService {

    private final AnalysisGptReportInputReadService analysisGptReportInputReadService;
    private final AnalysisLlmNarrativeInputAssembler analysisLlmNarrativeInputAssembler;

    public AnalysisLlmNarrativeInputPayload getLatestInput(String symbol, AnalysisReportType reportType) {
        return analysisLlmNarrativeInputAssembler.assemble(
                analysisGptReportInputReadService.getLatestInput(symbol, reportType)
        );
    }
}

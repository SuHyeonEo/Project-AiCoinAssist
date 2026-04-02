package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;

public interface AnalysisLlmNarrativeGateway {

    AnalysisLlmNarrativeGatewayResponse generate(AnalysisLlmNarrativeGatewayRequest request);
}

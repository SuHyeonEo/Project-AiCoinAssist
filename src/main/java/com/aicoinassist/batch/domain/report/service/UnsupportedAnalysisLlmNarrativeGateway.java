package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnsupportedAnalysisLlmNarrativeGateway implements AnalysisLlmNarrativeGateway {

    @Override
    public AnalysisLlmNarrativeGatewayResponse generate(AnalysisLlmNarrativeGatewayRequest request) {
        throw new AnalysisLlmNarrativeGatewayException(
                AnalysisLlmNarrativeFailureType.UNSUPPORTED,
                false,
                "No LLM narrative gateway provider is configured."
        );
    }
}

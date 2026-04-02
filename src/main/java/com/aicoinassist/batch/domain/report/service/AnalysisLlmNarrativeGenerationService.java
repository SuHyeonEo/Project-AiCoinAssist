package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputProcessingResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmRetryPolicy;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextResolution;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisLlmNarrativeGenerationService {

    private final AnalysisLlmNarrativeProperties analysisLlmNarrativeProperties;
    private final AnalysisGptReportInputReadService analysisGptReportInputReadService;
    private final AnalysisLlmSharedContextGenerationService analysisLlmSharedContextGenerationService;
    private final AnalysisLlmNarrativeInputAssembler analysisLlmNarrativeInputAssembler;
    private final AnalysisLlmPromptComposer analysisLlmPromptComposer;
    private final AnalysisLlmNarrativeGateway analysisLlmNarrativeGateway;
    private final AnalysisLlmOutputPostProcessor analysisLlmOutputPostProcessor;
    private final AnalysisLlmOutputFallbackFactory analysisLlmOutputFallbackFactory;

    public AnalysisLlmNarrativeGenerationResult generateLatest(
            String symbol,
            AnalysisReportType reportType
    ) {
        AnalysisGptReportInputPayload reportInput = analysisGptReportInputReadService.getLatestInput(symbol, reportType);
        AnalysisLlmSharedContextResolution sharedContextResolution =
                analysisLlmSharedContextGenerationService.getOrGenerate(reportInput);
        AnalysisLlmNarrativeInputPayload input =
                analysisLlmNarrativeInputAssembler.assemble(reportInput, sharedContextResolution.reference());
        AnalysisLlmPromptComposition composition = analysisLlmPromptComposer.compose(input);
        AnalysisLlmNarrativeGatewayRequest request = AnalysisLlmNarrativeGatewayRequest.from(composition);
        AnalysisLlmRetryPolicy retryPolicy = new AnalysisLlmRetryPolicy(analysisLlmNarrativeProperties.maxTransportAttempts());
        List<String> transportIssues = new ArrayList<>();
        AnalysisLlmNarrativeFailureType failureType = AnalysisLlmNarrativeFailureType.NONE;

        for (int attempt = 1; attempt <= retryPolicy.maxTransportAttempts(); attempt++) {
            try {
                AnalysisLlmNarrativeGatewayResponse gatewayResponse = analysisLlmNarrativeGateway.generate(request);
                AnalysisLlmOutputProcessingResult processingResult = analysisLlmOutputPostProcessor.process(
                        input,
                        gatewayResponse.rawOutputJson()
                );
                AnalysisLlmNarrativeFailureType resultFailureType =
                        processingResult.fallbackUsed() ? AnalysisLlmNarrativeFailureType.CONTENT : AnalysisLlmNarrativeFailureType.NONE;
                return new AnalysisLlmNarrativeGenerationResult(
                        sharedContextResolution,
                        composition,
                        gatewayResponse,
                        processingResult,
                        attempt,
                        processingResult.fallbackUsed(),
                        resultFailureType,
                        transportIssues
                );
            } catch (AnalysisLlmNarrativeGatewayException exception) {
                failureType = exception.getFailureType();
                transportIssues.add(exception.getMessage());
                if (!exception.isRetryable() || attempt >= retryPolicy.maxTransportAttempts()) {
                    return fallbackResult(sharedContextResolution, input, composition, attempt, failureType, transportIssues);
                }
            }
        }

        return fallbackResult(
                sharedContextResolution,
                input,
                composition,
                retryPolicy.maxTransportAttempts(),
                failureType == AnalysisLlmNarrativeFailureType.NONE ? AnalysisLlmNarrativeFailureType.UNKNOWN : failureType,
                transportIssues
        );
    }

    private AnalysisLlmNarrativeGenerationResult fallbackResult(
            AnalysisLlmSharedContextResolution sharedContextResolution,
            AnalysisLlmNarrativeInputPayload input,
            AnalysisLlmPromptComposition composition,
            int attempts,
            AnalysisLlmNarrativeFailureType failureType,
            List<String> transportIssues
    ) {
        return new AnalysisLlmNarrativeGenerationResult(
                sharedContextResolution,
                composition,
                null,
                new AnalysisLlmOutputProcessingResult(
                        analysisLlmOutputFallbackFactory.build(input),
                        true,
                        List.of("Fallback narrative output was used because the LLM response could not be used safely.")
                ),
                attempts,
                true,
                failureType,
                List.copyOf(transportIssues)
        );
    }
}

package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.config.ReferenceNewsProperties;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayRequest;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGenerationResult;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsPromptComposition;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsSnapshotPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReferenceNewsGenerationService {

    private final ReferenceNewsProperties referenceNewsProperties;
    private final ReferenceNewsPromptComposer referenceNewsPromptComposer;
    private final ReferenceNewsGateway referenceNewsGateway;
    private final ReferenceNewsOutputPostProcessor referenceNewsOutputPostProcessor;

    public ReferenceNewsGenerationResult generate(LocalDate snapshotDate) {
        ReferenceNewsPromptComposition composition = referenceNewsPromptComposer.compose(snapshotDate);
        ReferenceNewsGatewayRequest request = ReferenceNewsGatewayRequest.from(composition);

        ReferenceNewsGatewayException lastException = null;
        for (int attempt = 1; attempt <= referenceNewsProperties.maxTransportAttempts(); attempt++) {
            try {
                ReferenceNewsGatewayResponse gatewayResponse = referenceNewsGateway.generate(request);
                ReferenceNewsSnapshotPayload payload = referenceNewsOutputPostProcessor.process(gatewayResponse.rawOutputJson());
                return new ReferenceNewsGenerationResult(composition, gatewayResponse, payload, attempt);
            } catch (ReferenceNewsGatewayException exception) {
                lastException = exception;
                if (!exception.isRetryable() || attempt >= referenceNewsProperties.maxTransportAttempts()) {
                    throw exception;
                }
            }
        }

        throw lastException == null
                ? new IllegalStateException("Reference news generation failed without a concrete gateway exception.")
                : lastException;
    }
}

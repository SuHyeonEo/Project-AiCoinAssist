package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;

public class AnalysisLlmNarrativeGatewayException extends RuntimeException {

    private final AnalysisLlmNarrativeFailureType failureType;
    private final boolean retryable;

    public AnalysisLlmNarrativeGatewayException(
            AnalysisLlmNarrativeFailureType failureType,
            boolean retryable,
            String message
    ) {
        super(message);
        this.failureType = failureType;
        this.retryable = retryable;
    }

    public AnalysisLlmNarrativeGatewayException(
            AnalysisLlmNarrativeFailureType failureType,
            boolean retryable,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.failureType = failureType;
        this.retryable = retryable;
    }

    public AnalysisLlmNarrativeFailureType getFailureType() {
        return failureType;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

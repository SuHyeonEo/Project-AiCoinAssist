package com.aicoinassist.batch.domain.news.service;

import com.aicoinassist.batch.domain.news.enumtype.ReferenceNewsGenerationFailureType;

public class ReferenceNewsGatewayException extends RuntimeException {

    private final ReferenceNewsGenerationFailureType failureType;
    private final boolean retryable;

    public ReferenceNewsGatewayException(
            ReferenceNewsGenerationFailureType failureType,
            boolean retryable,
            String message
    ) {
        super(message);
        this.failureType = failureType;
        this.retryable = retryable;
    }

    public ReferenceNewsGatewayException(
            ReferenceNewsGenerationFailureType failureType,
            boolean retryable,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.failureType = failureType;
        this.retryable = retryable;
    }

    public ReferenceNewsGenerationFailureType getFailureType() {
        return failureType;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

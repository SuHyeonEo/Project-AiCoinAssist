package com.aicoinassist.batch.domain.news.enumtype;

public enum ReferenceNewsGenerationFailureType {
    NONE,
    UNSUPPORTED,
    NETWORK,
    TIMEOUT,
    RATE_LIMIT,
    PROVIDER_ERROR,
    CONTENT,
    UNKNOWN
}

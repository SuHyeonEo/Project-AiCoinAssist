package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmRetryPolicy(
        int maxTransportAttempts
) {

    public static AnalysisLlmRetryPolicy defaultPolicy() {
        return new AnalysisLlmRetryPolicy(2);
    }
}

package com.aicoinassist.batch.domain.onchain.enumtype;

public enum OnchainMetricType {
    ACTIVE_ADDRESS_COUNT("AdrActCnt"),
    TRANSACTION_COUNT("TxCnt"),
    MARKET_CAP_USD("CapMrktCurUSD");

    private final String coinMetricsMetricId;

    OnchainMetricType(String coinMetricsMetricId) {
        this.coinMetricsMetricId = coinMetricsMetricId;
    }

    public String coinMetricsMetricId() {
        return coinMetricsMetricId;
    }
}

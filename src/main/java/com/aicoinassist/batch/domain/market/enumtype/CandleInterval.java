package com.aicoinassist.batch.domain.market.enumtype;

public enum CandleInterval {
    ONE_HOUR("1h"),
    FOUR_HOUR("4h"),
    ONE_DAY("1d");

    private final String value;

    CandleInterval(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
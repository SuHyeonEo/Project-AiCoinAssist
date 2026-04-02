package com.aicoinassist.batch.domain.market.enumtype;

public enum MarketWindowType {
    LAST_1D(1),
    LAST_3D(3),
    LAST_7D(7),
    LAST_14D(14),
    LAST_30D(30),
    LAST_90D(90),
    LAST_180D(180),
    LAST_52W(364);

    private final long days;

    MarketWindowType(long days) {
        this.days = days;
    }

    public long days() {
        return days;
    }
}

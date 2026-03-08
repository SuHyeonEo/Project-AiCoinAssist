package com.aicoinassist.batch.domain.market.enumtype;

public enum AssetType {
    BTC("BTCUSDT"),
    ETH("ETHUSDT"),
    XRP("XRPUSDT");

    private final String symbol;

    AssetType(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}

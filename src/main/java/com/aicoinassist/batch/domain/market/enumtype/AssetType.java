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

    public static AssetType fromSymbol(String symbol) {
        for (AssetType assetType : values()) {
            if (assetType.symbol.equals(symbol)) {
                return assetType;
            }
        }
        throw new IllegalArgumentException("Unsupported asset symbol: " + symbol);
    }
}

package com.aicoinassist.batch.domain.market.enumtype;

public enum AssetType {
    BTC("BTCUSDT", "btc"),
    ETH("ETHUSDT", "eth"),
    XRP("XRPUSDT", "xrp");

    private final String symbol;
    private final String onchainAssetCode;

    AssetType(String symbol, String onchainAssetCode) {
        this.symbol = symbol;
        this.onchainAssetCode = onchainAssetCode;
    }

    public String symbol() {
        return symbol;
    }

    public String onchainAssetCode() {
        return onchainAssetCode;
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

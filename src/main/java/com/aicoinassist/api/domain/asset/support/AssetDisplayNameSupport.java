package com.aicoinassist.api.domain.asset.support;

import java.util.Comparator;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AssetDisplayNameSupport {

	private static final Map<String, String> DISPLAY_NAMES = Map.of(
		"BTCUSDT", "Bitcoin",
		"ETHUSDT", "Ethereum",
		"XRPUSDT", "XRP"
	);

	private static final Map<String, Integer> DISPLAY_ORDER = Map.of(
		"BTCUSDT", 1,
		"ETHUSDT", 2,
		"XRPUSDT", 3
	);

	public String assetCode(String symbol) {
		return symbol != null && symbol.endsWith("USDT")
			? symbol.substring(0, symbol.length() - 4)
			: symbol;
	}

	public String assetName(String symbol) {
		return DISPLAY_NAMES.getOrDefault(symbol, assetCode(symbol));
	}

	public Comparator<String> symbolComparator() {
		return Comparator
			.comparingInt((String symbol) -> DISPLAY_ORDER.getOrDefault(symbol, Integer.MAX_VALUE))
			.thenComparing(symbol -> symbol);
	}
}

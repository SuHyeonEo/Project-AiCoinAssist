package com.aicoinassist.batch.domain.news.support;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class NewsAssetKeywordSupport {

    public String assetCode(AssetType assetType) {
        return assetType.name().toLowerCase(Locale.ROOT);
    }

    public List<String> keywords(AssetType assetType) {
        return switch (assetType) {
            case BTC -> List.of("bitcoin", "btc");
            case ETH -> List.of("ethereum", "eth", "ether");
            case XRP -> List.of("xrp", "ripple");
        };
    }

    public String queryText(AssetType assetType) {
        return String.join(" OR ", keywords(assetType));
    }
}

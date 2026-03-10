package com.aicoinassist.batch.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.fred")
public record FredProperties(
        String baseUrl,
        String apiKey,
        Series series
) {

    public FredProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.stlouisfed.org"
                : baseUrl;
        series = series == null ? new Series(null, null, null) : series;
    }

    public record Series(
            String dxyProxy,
            String us10yYield,
            String usdKrw
    ) {

        public Series {
            dxyProxy = dxyProxy == null || dxyProxy.isBlank()
                    ? "DTWEXBGS"
                    : dxyProxy;
            us10yYield = us10yYield == null || us10yYield.isBlank()
                    ? "DGS10"
                    : us10yYield;
            usdKrw = usdKrw == null || usdKrw.isBlank()
                    ? "DEXKOUS"
                    : usdKrw;
        }
    }
}

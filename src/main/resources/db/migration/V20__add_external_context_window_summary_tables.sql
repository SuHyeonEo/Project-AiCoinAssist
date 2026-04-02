CREATE TABLE sentiment_window_summary_snapshot
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_type              VARCHAR(50)    NOT NULL,
    window_type              VARCHAR(20)    NOT NULL,
    window_start_time        TIMESTAMP(6)   NOT NULL,
    window_end_time          TIMESTAMP(6)   NOT NULL,
    sample_count             INT            NOT NULL,
    current_index_value      DECIMAL(19, 8) NOT NULL,
    average_index_value      DECIMAL(19, 8) NOT NULL,
    current_index_vs_average DECIMAL(19, 8) NULL,
    current_classification   VARCHAR(50)    NOT NULL,
    greed_sample_count       INT            NOT NULL,
    fear_sample_count        INT            NOT NULL,
    source_data_version      VARCHAR(300)   NOT NULL,
    CONSTRAINT uk_sentiment_window_summary_metric_window_end
        UNIQUE (metric_type, window_type, window_end_time)
);

CREATE INDEX idx_sentiment_window_summary_metric_window_end
    ON sentiment_window_summary_snapshot (metric_type, window_end_time);

CREATE INDEX idx_sentiment_window_summary_source_data_version
    ON sentiment_window_summary_snapshot (source_data_version);

CREATE TABLE macro_context_window_summary_snapshot
(
    id                            BIGINT AUTO_INCREMENT PRIMARY KEY,
    window_type                   VARCHAR(20)    NOT NULL,
    window_start_time             TIMESTAMP(6)   NOT NULL,
    window_end_time               TIMESTAMP(6)   NOT NULL,
    sample_count                  INT            NOT NULL,
    current_dxy_proxy_value       DECIMAL(19, 8) NOT NULL,
    average_dxy_proxy_value       DECIMAL(19, 8) NOT NULL,
    current_dxy_proxy_vs_average  DECIMAL(19, 8) NULL,
    current_us10y_yield_value     DECIMAL(19, 8) NOT NULL,
    average_us10y_yield_value     DECIMAL(19, 8) NOT NULL,
    current_us10y_yield_vs_average DECIMAL(19, 8) NULL,
    current_usd_krw_value         DECIMAL(19, 8) NOT NULL,
    average_usd_krw_value         DECIMAL(19, 8) NOT NULL,
    current_usd_krw_vs_average    DECIMAL(19, 8) NULL,
    source_data_version           VARCHAR(300)   NOT NULL,
    CONSTRAINT uk_macro_context_window_summary_window_end
        UNIQUE (window_type, window_end_time)
);

CREATE INDEX idx_macro_context_window_summary_window_end
    ON macro_context_window_summary_snapshot (window_end_time);

CREATE INDEX idx_macro_context_window_summary_source_data_version
    ON macro_context_window_summary_snapshot (source_data_version);

CREATE TABLE onchain_window_summary_snapshot
(
    id                             BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol                         VARCHAR(20)    NOT NULL,
    asset_code                     VARCHAR(20)    NOT NULL,
    window_type                    VARCHAR(20)    NOT NULL,
    window_start_time              TIMESTAMP(6)   NOT NULL,
    window_end_time                TIMESTAMP(6)   NOT NULL,
    sample_count                   INT            NOT NULL,
    current_active_address_count   DECIMAL(19, 8) NOT NULL,
    average_active_address_count   DECIMAL(19, 8) NOT NULL,
    current_active_address_vs_average DECIMAL(19, 8) NULL,
    current_transaction_count      DECIMAL(19, 8) NOT NULL,
    average_transaction_count      DECIMAL(19, 8) NOT NULL,
    current_transaction_count_vs_average DECIMAL(19, 8) NULL,
    current_market_cap_usd         DECIMAL(24, 8) NOT NULL,
    average_market_cap_usd         DECIMAL(24, 8) NOT NULL,
    current_market_cap_vs_average  DECIMAL(19, 8) NULL,
    source_data_version            VARCHAR(300)   NOT NULL,
    CONSTRAINT uk_onchain_window_summary_symbol_window_end
        UNIQUE (symbol, window_type, window_end_time)
);

CREATE INDEX idx_onchain_window_summary_symbol_window_end
    ON onchain_window_summary_snapshot (symbol, window_end_time);

CREATE INDEX idx_onchain_window_summary_source_data_version
    ON onchain_window_summary_snapshot (source_data_version);

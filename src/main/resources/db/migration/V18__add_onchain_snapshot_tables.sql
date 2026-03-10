CREATE TABLE onchain_snapshot_raw
(
    id                            BIGINT AUTO_INCREMENT PRIMARY KEY,
    source                        VARCHAR(50)    NOT NULL,
    asset_code                    VARCHAR(20)    NOT NULL,
    metric_type                   VARCHAR(50)    NOT NULL,
    source_event_time             TIMESTAMP(6)   NULL,
    collected_time                TIMESTAMP(6)   NOT NULL,
    validation_status             VARCHAR(20)    NOT NULL,
    validation_details            TEXT           NULL,
    metric_value                  DECIMAL(19, 8) NULL,
    raw_payload                   TEXT           NOT NULL,
    CONSTRAINT uk_onchain_snapshot_raw_source_asset_metric_source_event_time
        UNIQUE (source, asset_code, metric_type, source_event_time)
);

CREATE INDEX idx_onchain_snapshot_raw_asset_metric_collected_time
    ON onchain_snapshot_raw (asset_code, metric_type, collected_time);

CREATE TABLE onchain_fact_snapshot
(
    id                                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol                              VARCHAR(20)    NOT NULL,
    asset_code                          VARCHAR(20)    NOT NULL,
    snapshot_time                       TIMESTAMP(6)   NOT NULL,
    active_address_source_event_time    TIMESTAMP(6)   NOT NULL,
    transaction_count_source_event_time TIMESTAMP(6)   NOT NULL,
    market_cap_source_event_time        TIMESTAMP(6)   NOT NULL,
    source_data_version                 VARCHAR(250)   NOT NULL,
    active_address_count                DECIMAL(19, 8) NOT NULL,
    transaction_count                   DECIMAL(19, 8) NOT NULL,
    market_cap_usd                      DECIMAL(24, 8) NOT NULL,
    CONSTRAINT uk_onchain_fact_snapshot_symbol_snapshot_time
        UNIQUE (symbol, snapshot_time)
);

CREATE INDEX idx_onchain_fact_snapshot_symbol_snapshot_time
    ON onchain_fact_snapshot (symbol, snapshot_time);

CREATE INDEX idx_onchain_fact_snapshot_source_data_version
    ON onchain_fact_snapshot (source_data_version);

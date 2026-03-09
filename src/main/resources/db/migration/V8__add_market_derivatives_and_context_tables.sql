CREATE TABLE market_open_interest_raw (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    source_event_time TIMESTAMP(6) NULL,
    collected_time TIMESTAMP(6) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    validation_details VARCHAR(500) NULL,
    open_interest DECIMAL(19, 8) NULL,
    raw_payload TEXT NOT NULL,
    CONSTRAINT pk_market_open_interest_raw PRIMARY KEY (id),
    CONSTRAINT uk_market_open_interest_raw_source_symbol_source_event_time
        UNIQUE (source, symbol, source_event_time)
);

CREATE INDEX idx_market_open_interest_raw_symbol_collected_time
    ON market_open_interest_raw (symbol, collected_time);

CREATE TABLE market_premium_index_raw (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    source_event_time TIMESTAMP(6) NULL,
    collected_time TIMESTAMP(6) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    validation_details VARCHAR(500) NULL,
    mark_price DECIMAL(19, 8) NULL,
    index_price DECIMAL(19, 8) NULL,
    last_funding_rate DECIMAL(19, 8) NULL,
    next_funding_time TIMESTAMP(6) NULL,
    raw_payload TEXT NOT NULL,
    CONSTRAINT pk_market_premium_index_raw PRIMARY KEY (id),
    CONSTRAINT uk_market_premium_index_raw_source_symbol_source_event_time
        UNIQUE (source, symbol, source_event_time)
);

CREATE INDEX idx_market_premium_index_raw_symbol_collected_time
    ON market_premium_index_raw (symbol, collected_time);

CREATE TABLE market_context_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    snapshot_time TIMESTAMP(6) NOT NULL,
    open_interest_source_event_time TIMESTAMP(6) NOT NULL,
    premium_index_source_event_time TIMESTAMP(6) NOT NULL,
    source_data_version VARCHAR(200) NOT NULL,
    open_interest DECIMAL(19, 8) NOT NULL,
    mark_price DECIMAL(19, 8) NOT NULL,
    index_price DECIMAL(19, 8) NOT NULL,
    last_funding_rate DECIMAL(19, 8) NOT NULL,
    next_funding_time TIMESTAMP(6) NOT NULL,
    mark_index_basis_rate DECIMAL(19, 8) NOT NULL,
    CONSTRAINT pk_market_context_snapshot PRIMARY KEY (id),
    CONSTRAINT uk_market_context_snapshot_symbol_snapshot_time
        UNIQUE (symbol, snapshot_time)
);

CREATE INDEX idx_market_context_snapshot_symbol_snapshot_time
    ON market_context_snapshot (symbol, snapshot_time);

CREATE INDEX idx_market_context_snapshot_source_data_version
    ON market_context_snapshot (source_data_version);

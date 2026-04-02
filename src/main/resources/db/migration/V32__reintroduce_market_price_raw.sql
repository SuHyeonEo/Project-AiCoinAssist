CREATE TABLE market_price_raw (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    source_event_time TIMESTAMP(6) NULL,
    collected_time TIMESTAMP(6) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    validation_details VARCHAR(500) NULL,
    price DECIMAL(19, 8) NULL,
    raw_payload TEXT NOT NULL,
    CONSTRAINT pk_market_price_raw PRIMARY KEY (id),
    CONSTRAINT uk_market_price_raw_source_symbol_source_event_time
        UNIQUE (source, symbol, source_event_time)
);

CREATE INDEX idx_market_price_raw_symbol_collected_time
    ON market_price_raw (symbol, collected_time);

CREATE INDEX idx_market_price_raw_symbol_source_event_time
    ON market_price_raw (symbol, source_event_time);

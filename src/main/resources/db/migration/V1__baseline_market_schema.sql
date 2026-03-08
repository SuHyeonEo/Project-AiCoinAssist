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

CREATE TABLE market_candle_raw (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    interval_value VARCHAR(20) NOT NULL,
    open_time TIMESTAMP(6) NULL,
    close_time TIMESTAMP(6) NULL,
    open_price DECIMAL(19, 8) NULL,
    high_price DECIMAL(19, 8) NULL,
    low_price DECIMAL(19, 8) NULL,
    close_price DECIMAL(19, 8) NULL,
    volume DECIMAL(19, 8) NULL,
    collected_time TIMESTAMP(6) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    validation_details VARCHAR(500) NULL,
    raw_payload TEXT NOT NULL,
    CONSTRAINT pk_market_candle_raw PRIMARY KEY (id),
    CONSTRAINT uk_market_candle_raw_source_symbol_interval_open_time
        UNIQUE (source, symbol, interval_value, open_time)
);

CREATE INDEX idx_market_candle_raw_symbol_interval_collected_time
    ON market_candle_raw (symbol, interval_value, collected_time);

CREATE TABLE market_indicator_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    interval_value VARCHAR(20) NOT NULL,
    snapshot_time TIMESTAMP(6) NOT NULL,
    current_price DECIMAL(19, 8) NOT NULL,
    ma20 DECIMAL(19, 8) NOT NULL,
    ma60 DECIMAL(19, 8) NOT NULL,
    ma120 DECIMAL(19, 8) NOT NULL,
    rsi14 DECIMAL(19, 8) NOT NULL,
    macd_line DECIMAL(19, 8) NOT NULL,
    macd_signal_line DECIMAL(19, 8) NOT NULL,
    macd_histogram DECIMAL(19, 8) NOT NULL,
    atr14 DECIMAL(19, 8) NOT NULL,
    bollinger_upper_band DECIMAL(19, 8) NOT NULL,
    bollinger_middle_band DECIMAL(19, 8) NOT NULL,
    bollinger_lower_band DECIMAL(19, 8) NOT NULL,
    CONSTRAINT pk_market_indicator_snapshot PRIMARY KEY (id)
);

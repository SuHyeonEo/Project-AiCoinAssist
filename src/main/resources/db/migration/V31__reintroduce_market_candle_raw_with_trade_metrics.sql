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
    volume DECIMAL(24, 8) NULL,
    quote_asset_volume DECIMAL(24, 8) NULL,
    number_of_trades BIGINT NULL,
    taker_buy_base_asset_volume DECIMAL(24, 8) NULL,
    taker_buy_quote_asset_volume DECIMAL(24, 8) NULL,
    collected_time TIMESTAMP(6) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    validation_details VARCHAR(500) NULL,
    raw_payload TEXT NOT NULL,
    CONSTRAINT pk_market_candle_raw PRIMARY KEY (id),
    CONSTRAINT uk_market_candle_raw_source_symbol_interval_open_time
        UNIQUE (source, symbol, interval_value, open_time)
);

CREATE INDEX idx_market_candle_raw_symbol_interval_open_time ON market_candle_raw (symbol, interval_value, open_time);
CREATE INDEX idx_market_candle_raw_symbol_interval_close_time ON market_candle_raw (symbol, interval_value, close_time);

ALTER TABLE market_indicator_snapshot
    ADD CONSTRAINT uk_market_indicator_snapshot_symbol_interval_snapshot_time
        UNIQUE (symbol, interval_value, snapshot_time);

CREATE INDEX idx_market_indicator_snapshot_symbol_interval_snapshot_time
    ON market_indicator_snapshot (symbol, interval_value, snapshot_time);

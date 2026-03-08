ALTER TABLE market_indicator_snapshot
    ADD COLUMN latest_candle_open_time TIMESTAMP(6) NULL;

ALTER TABLE market_indicator_snapshot
    ADD COLUMN price_source_event_time TIMESTAMP(6) NULL;

UPDATE market_indicator_snapshot
SET latest_candle_open_time = snapshot_time,
    price_source_event_time = snapshot_time
WHERE latest_candle_open_time IS NULL
   OR price_source_event_time IS NULL;

ALTER TABLE market_indicator_snapshot
    ALTER COLUMN latest_candle_open_time SET NOT NULL;

ALTER TABLE market_indicator_snapshot
    ALTER COLUMN price_source_event_time SET NOT NULL;

CREATE INDEX idx_market_indicator_snapshot_price_source_event_time
    ON market_indicator_snapshot (price_source_event_time);

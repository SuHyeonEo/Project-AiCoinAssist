ALTER TABLE market_indicator_snapshot
    ADD COLUMN source_data_version VARCHAR(200) NULL;

UPDATE market_indicator_snapshot
SET source_data_version = CONCAT(
        'snapshotTime=', snapshot_time,
        ';latestCandleOpenTime=', latest_candle_open_time,
        ';priceSourceEventTime=', price_source_event_time
    )
WHERE source_data_version IS NULL;

ALTER TABLE market_indicator_snapshot
    ALTER COLUMN source_data_version SET NOT NULL;

CREATE INDEX idx_market_indicator_snapshot_source_data_version
    ON market_indicator_snapshot (source_data_version);

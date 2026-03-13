ALTER TABLE market_window_summary_snapshot
    ADD COLUMN average_quote_asset_volume DECIMAL(24, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN average_trade_count DECIMAL(19, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN current_quote_asset_volume DECIMAL(24, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN current_trade_count DECIMAL(19, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN current_quote_asset_volume_vs_average DECIMAL(19, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN current_trade_count_vs_average DECIMAL(19, 8) NULL;

ALTER TABLE market_window_summary_snapshot
    ADD COLUMN current_taker_buy_quote_ratio DECIMAL(19, 8) NULL;

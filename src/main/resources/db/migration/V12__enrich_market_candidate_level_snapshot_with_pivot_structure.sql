ALTER TABLE market_candidate_level_snapshot
    ADD COLUMN reference_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE market_candidate_level_snapshot
    ADD COLUMN reaction_count INT NOT NULL DEFAULT 0;

ALTER TABLE market_candidate_level_snapshot
    ADD COLUMN cluster_size INT NOT NULL DEFAULT 1;

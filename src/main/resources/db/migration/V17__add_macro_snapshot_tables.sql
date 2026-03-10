CREATE TABLE macro_snapshot_raw
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    source             VARCHAR(50)    NOT NULL,
    metric_type        VARCHAR(50)    NOT NULL,
    series_id          VARCHAR(50)    NULL,
    units              VARCHAR(50)    NULL,
    observation_date   DATE           NULL,
    collected_time     TIMESTAMP(6)   NOT NULL,
    validation_status  VARCHAR(20)    NOT NULL,
    validation_details TEXT           NULL,
    metric_value       DECIMAL(19, 8) NULL,
    raw_payload        TEXT           NOT NULL,
    CONSTRAINT uk_macro_snapshot_raw_source_metric_observation_date UNIQUE (source, metric_type, observation_date)
);

CREATE INDEX idx_macro_snapshot_raw_metric_collected_time
    ON macro_snapshot_raw (metric_type, collected_time);

CREATE TABLE macro_context_snapshot
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_time                TIMESTAMP(6)   NOT NULL,
    dxy_observation_date         DATE           NOT NULL,
    us10y_yield_observation_date DATE           NOT NULL,
    usd_krw_observation_date     DATE           NOT NULL,
    source_data_version          VARCHAR(200)   NOT NULL,
    dxy_proxy_value              DECIMAL(19, 8) NOT NULL,
    us10y_yield_value            DECIMAL(19, 8) NOT NULL,
    usd_krw_value                DECIMAL(19, 8) NOT NULL,
    CONSTRAINT uk_macro_context_snapshot_snapshot_time UNIQUE (snapshot_time)
);

CREATE INDEX idx_macro_context_snapshot_snapshot_time
    ON macro_context_snapshot (snapshot_time);

CREATE INDEX idx_macro_context_snapshot_source_data_version
    ON macro_context_snapshot (source_data_version);

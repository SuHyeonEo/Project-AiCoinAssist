CREATE TABLE news_signal_raw
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    source             VARCHAR(50)   NOT NULL,
    asset_code         VARCHAR(20)   NOT NULL,
    query_text         VARCHAR(200)  NOT NULL,
    seen_time          TIMESTAMP(6)  NULL,
    article_url        VARCHAR(512)  NULL,
    mobile_url         VARCHAR(512)  NULL,
    title              VARCHAR(500)  NULL,
    domain             VARCHAR(100)  NULL,
    source_language    VARCHAR(20)   NULL,
    source_country     VARCHAR(20)   NULL,
    social_image_url   VARCHAR(512)  NULL,
    collected_time     TIMESTAMP(6)  NOT NULL,
    validation_status  VARCHAR(20)   NOT NULL,
    validation_details TEXT          NULL,
    raw_payload        TEXT          NOT NULL,
    CONSTRAINT uk_news_signal_raw_source_asset_article_url
        UNIQUE (source, asset_code, article_url)
);

CREATE INDEX idx_news_signal_raw_asset_seen_time
    ON news_signal_raw (asset_code, seen_time);

CREATE TABLE news_signal_snapshot
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol                  VARCHAR(20)    NOT NULL,
    asset_code              VARCHAR(20)    NOT NULL,
    snapshot_time           TIMESTAMP(6)   NOT NULL,
    seen_time               TIMESTAMP(6)   NOT NULL,
    source_data_version     VARCHAR(200)   NOT NULL,
    article_url             VARCHAR(512)   NOT NULL,
    title                   VARCHAR(500)   NOT NULL,
    domain                  VARCHAR(100)   NULL,
    source_language         VARCHAR(20)    NULL,
    source_country          VARCHAR(20)    NULL,
    title_keyword_hit_count INT            NOT NULL,
    priority_score          DECIMAL(10, 4) NOT NULL,
    CONSTRAINT uk_news_signal_snapshot_symbol_article_url
        UNIQUE (symbol, article_url)
);

CREATE INDEX idx_news_signal_snapshot_symbol_snapshot_time
    ON news_signal_snapshot (symbol, snapshot_time);

CREATE INDEX idx_news_signal_snapshot_source_data_version
    ON news_signal_snapshot (source_data_version);

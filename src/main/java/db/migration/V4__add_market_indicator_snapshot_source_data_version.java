package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class V4__add_market_indicator_snapshot_source_data_version extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            if (!columnExists(context, "market_indicator_snapshot", "source_data_version")) {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ADD COLUMN source_data_version VARCHAR(200) NULL
                        """);
            }

            statement.execute("""
                    UPDATE market_indicator_snapshot
                    SET source_data_version = CONCAT(
                            'snapshotTime=', snapshot_time,
                            ';latestCandleOpenTime=', latest_candle_open_time,
                            ';priceSourceEventTime=', price_source_event_time
                        )
                    WHERE source_data_version IS NULL
                    """);

            String databaseProductName = context.getConnection().getMetaData().getDatabaseProductName();
            if ("H2".equalsIgnoreCase(databaseProductName)) {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ALTER COLUMN source_data_version SET NOT NULL
                        """);
            } else {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            MODIFY COLUMN source_data_version VARCHAR(200) NOT NULL
                        """);
            }

            if (!indexExists(context, "market_indicator_snapshot", "idx_market_indicator_snapshot_source_data_version")) {
                statement.execute("""
                        CREATE INDEX idx_market_indicator_snapshot_source_data_version
                            ON market_indicator_snapshot (source_data_version)
                        """);
            }
        }
    }

    private boolean columnExists(Context context, String tableName, String columnName) throws Exception {
        DatabaseMetaData metadata = context.getConnection().getMetaData();
        try (ResultSet resultSet = metadata.getColumns(null, null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private boolean indexExists(Context context, String tableName, String indexName) throws Exception {
        DatabaseMetaData metadata = context.getConnection().getMetaData();
        try (ResultSet resultSet = metadata.getIndexInfo(null, null, tableName, false, false)) {
            while (resultSet.next()) {
                String existingIndexName = resultSet.getString("INDEX_NAME");
                if (indexName.equalsIgnoreCase(existingIndexName)) {
                    return true;
                }
            }
        }
        return false;
    }
}

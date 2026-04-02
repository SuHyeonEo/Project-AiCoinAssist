package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class V3__add_market_indicator_snapshot_basis_metadata extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            if (!columnExists(context, "market_indicator_snapshot", "latest_candle_open_time")) {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ADD COLUMN latest_candle_open_time TIMESTAMP(6) NULL
                        """);
            }
            if (!columnExists(context, "market_indicator_snapshot", "price_source_event_time")) {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ADD COLUMN price_source_event_time TIMESTAMP(6) NULL
                        """);
            }
            statement.execute("""
                    UPDATE market_indicator_snapshot
                    SET latest_candle_open_time = snapshot_time,
                        price_source_event_time = snapshot_time
                    WHERE latest_candle_open_time IS NULL
                       OR price_source_event_time IS NULL
                    """);

            String databaseProductName = context.getConnection().getMetaData().getDatabaseProductName();
            if ("H2".equalsIgnoreCase(databaseProductName)) {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ALTER COLUMN latest_candle_open_time SET NOT NULL
                        """);
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            ALTER COLUMN price_source_event_time SET NOT NULL
                        """);
            } else {
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            MODIFY COLUMN latest_candle_open_time TIMESTAMP(6) NOT NULL
                        """);
                statement.execute("""
                        ALTER TABLE market_indicator_snapshot
                            MODIFY COLUMN price_source_event_time TIMESTAMP(6) NOT NULL
                        """);
            }

            if (!indexExists(context, "market_indicator_snapshot", "idx_market_indicator_snapshot_price_source_event_time")) {
                statement.execute("""
                        CREATE INDEX idx_market_indicator_snapshot_price_source_event_time
                            ON market_indicator_snapshot (price_source_event_time)
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

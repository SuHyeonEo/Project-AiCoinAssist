package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V24__expand_onchain_snapshot_raw_metric_value_precision extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            String databaseProductName = context.getConnection().getMetaData().getDatabaseProductName();
            if ("H2".equalsIgnoreCase(databaseProductName)) {
                statement.execute("""
                        ALTER TABLE onchain_snapshot_raw
                            ALTER COLUMN metric_value DECIMAL(24, 8)
                        """);
            } else {
                statement.execute("""
                        ALTER TABLE onchain_snapshot_raw
                            MODIFY COLUMN metric_value DECIMAL(24, 8) NULL
                        """);
            }
        }
    }
}

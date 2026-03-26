package com.aicoinassist.api.infra.db;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("db")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.db-inspection", name = "enabled", havingValue = "true")
public class DatabaseSchemaInspectionRunner implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		List<String> tableNames = jdbcTemplate.queryForList(
			"""
				select table_name
				from information_schema.tables
				where table_schema = database()
				order by table_name
				""",
			String.class
		);

		if (tableNames.isEmpty()) {
			log.warn("No tables found in the active database schema.");
			return;
		}

		log.info("Database schema inspection started. tableCount={}", tableNames.size());

		for (String tableName : tableNames) {
			List<String> columnDescriptions = jdbcTemplate.query(
				"""
					select concat(
						column_name,
						' ',
						column_type,
						' nullable=',
						is_nullable,
						' key=',
						ifnull(column_key, '')
					)
					from information_schema.columns
					where table_schema = database()
					  and table_name = ?
					order by ordinal_position
					""",
				(rs, rowNum) -> rs.getString(1),
				tableName
			);

			log.info("Table {} -> {}", tableName, String.join(", ", columnDescriptions));
		}
	}
}

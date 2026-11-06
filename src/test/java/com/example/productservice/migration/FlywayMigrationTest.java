package com.example.productservice.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that Flyway applies the product schema during application startup. */
@SpringBootTest
class FlywayMigrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void appliesTheProductsSchemaMigration() {
		Integer migrationCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = '1'", Integer.class);
		Integer tableCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PRODUCTS'", Integer.class);

		assertThat(migrationCount).isEqualTo(1);
		assertThat(tableCount).isEqualTo(1);
	}
}

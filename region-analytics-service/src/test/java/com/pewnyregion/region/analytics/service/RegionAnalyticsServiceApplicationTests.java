package com.pewnyregion.region.analytics.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"bdl.api.key=test-api-key",
})
@Testcontainers
class RegionAnalyticsServiceApplicationTests {

	@Configuration
	static class TestConfig {
		@Bean
		@ServiceConnection
		public PostgreSQLContainer postgresContainer() {
			return new PostgreSQLContainer("postgres:16");
		}
	}

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldLoadApplicationProperties() {
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("region-analytics-service");
		assertThat(environment.getProperty("bdl.api.key")).isEqualTo("test-api-key");
		assertThat(environment.getProperty("bdl.api.url")).isEqualTo("https://bdl.stat.gov.pl/api/v1");
	}
}

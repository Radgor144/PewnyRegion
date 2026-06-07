package com.pewnyregion.region.analytics.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"bdl.api.key=test-api-key",
})
@Testcontainers
class RegionAnalyticsServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

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

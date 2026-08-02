package com.pewnyregion.region.analytics.service;

import com.pewnyregion.region.analytics.service.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;


class RegionAnalyticsServiceApplicationTests extends AbstractIntegrationTest {

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

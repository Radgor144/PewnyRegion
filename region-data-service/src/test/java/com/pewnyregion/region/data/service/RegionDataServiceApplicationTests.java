package com.pewnyregion.region.data.service;

import com.pewnyregion.region.data.service.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;


class RegionDataServiceApplicationTests extends AbstractIntegrationTest {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldLoadApplicationProperties() {
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("region-data-service");
		assertThat(environment.getProperty("bdl.api.key")).isEqualTo("test-api-key");
		assertThat(environment.getProperty("bdl.api.url")).isEqualTo("https://bdl.stat.gov.pl/api/v1");
	}
}

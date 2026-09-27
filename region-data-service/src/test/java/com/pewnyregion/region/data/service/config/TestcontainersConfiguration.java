package com.pewnyregion.region.data.service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(PostgresTestContainers.class)
public class TestcontainersConfiguration {
}
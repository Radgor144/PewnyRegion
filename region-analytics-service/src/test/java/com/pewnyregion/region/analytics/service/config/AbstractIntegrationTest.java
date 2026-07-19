package com.pewnyregion.region.analytics.service.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "bdl.api.key=test-api-key"
        }
)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16");

    static {
        postgresContainer.start();
    }

    @Autowired
    private ConnectionFactory connectionFactory;

    protected void runSqlScript(String classpathResourcePath) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource(classpathResourcePath)
        );
        populator.populate(connectionFactory).block();
    }
}
package com.pewnyregion.region.data.service.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "bdl.api.key=test-api-key"
)
@Testcontainers
@DirtiesContext
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer(
            DockerImageName.parse("cgr.dev/chainguard/postgres:latest")
                           .asCompatibleSubstituteFor("postgres")
    ).withCommand();

    @Autowired
    private ConnectionFactory connectionFactory;

    protected void runSqlScript(String classpathResourcePath) {
        new ResourceDatabasePopulator(new ClassPathResource(classpathResourcePath))
                .populate(connectionFactory)
                .block();
    }
}
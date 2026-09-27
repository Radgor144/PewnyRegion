package com.pewnyregion.region.data.service.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "bdl.api.key=test-api-key"
)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    protected void runSqlScript(String classpathResourcePath) {
        new ResourceDatabasePopulator(new ClassPathResource(classpathResourcePath))
                .populate(connectionFactory)
                .block();
    }
}
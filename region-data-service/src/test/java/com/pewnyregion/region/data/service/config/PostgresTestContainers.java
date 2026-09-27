package com.pewnyregion.region.data.service.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

interface PostgresTestContainers {

    @Container
    @ServiceConnection
    PostgreSQLContainer postgresContainer = new PostgreSQLContainer(
            DockerImageName.parse("cgr.dev/chainguard/postgres:latest")
                           .asCompatibleSubstituteFor("postgres")
    ).withCommand();
}
package com.bwa.controlling;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Basisklasse für Integrationstests gegen ein echtes PostgreSQL.
 *
 * Singleton-Container-Pattern: der Container wird genau einmal pro JVM gestartet und über alle
 * Testklassen geteilt. Das ist nötig, weil Spring den ApplicationContext klassenübergreifend cached —
 * ein per-Klasse gestoppter Container (@Testcontainers/@Container) würde den gecachten Context auf
 * einen toten Port zeigen lassen.
 */
public abstract class AbstractPostgresIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}

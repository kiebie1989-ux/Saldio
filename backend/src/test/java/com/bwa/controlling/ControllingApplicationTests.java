package com.bwa.controlling;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Beweist, dass der Spring-Kontext samt JPA + Flyway-Migration gegen echtes Postgres hochfährt.
 */
@SpringBootTest
class ControllingApplicationTests extends AbstractPostgresIT {

    @Test
    void contextLoads() {
        // Schlägt fehl, wenn Auto-Config, Flyway-Migration oder JPA-Validierung bricht.
    }
}

package com.bwa.controlling.api;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End-Beweis des Skeletts: HTTP-Request -> Controller -> echte Datenbank.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointIT extends AbstractPostgresIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void fachlicherHealthEndpointMeldetDatenbankUp() {
        ResponseEntity<HealthController.HealthResponse> response =
                rest.getForEntity("/api/health", HealthController.HealthResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().application()).isEqualTo("controlling-backend");
        assertThat(response.getBody().database()).isEqualTo("UP");
    }

    @Test
    void actuatorHealthIstErreichbar() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}

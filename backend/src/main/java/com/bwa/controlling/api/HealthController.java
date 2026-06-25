package com.bwa.controlling.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Schlanker fachlicher Health-Endpoint (zusätzlich zu /actuator/health).
 * Dient im Skelett als End-to-End-Beweis: HTTP -> Spring -> Datenbank.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final String appName;

    public HealthController(JdbcTemplate jdbcTemplate,
                            @Value("${spring.application.name:controlling-backend}") String appName) {
        this.jdbcTemplate = jdbcTemplate;
        this.appName = appName;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        boolean dbUp = Integer.valueOf(1).equals(one);
        return new HealthResponse(appName, "0.1.0-SNAPSHOT", dbUp ? "UP" : "DOWN");
    }

    public record HealthResponse(String application, String version, String database) {}
}

package com.bwa.controlling.security;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Härtung: Security-Header und CORS-Policy. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityHardeningIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;

    @Test
    void setztSicherheitsHeader() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "same-origin"));
    }

    @Test
    void corsErlaubtKonfiguriertenOrigin() throws Exception {
        mockMvc.perform(options("/api/guv")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void corsLehntFremdenOriginAb() throws Exception {
        mockMvc.perform(options("/api/guv")
                        .header(HttpHeaders.ORIGIN, "https://boese.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}

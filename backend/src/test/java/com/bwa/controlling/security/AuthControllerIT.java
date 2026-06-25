package com.bwa.controlling.security;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorisierungsregeln des Resource-Servers — schnell mit Mock-JWT (ohne echtes Keycloak).
 * Der echte Token-Flow gegen Keycloak wird im Live-Smoke geprüft.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;

    @Test
    void whoamiOhneTokenIst401() throws Exception {
        mockMvc.perform(get("/api/auth/whoami")).andExpect(status().isUnauthorized());
    }

    @Test
    void whoamiMitTokenLiefertBenutzerUndRollen() throws Exception {
        mockMvc.perform(get("/api/auth/whoami").with(jwt()
                        .jwt(j -> j.subject("u-123").claim("preferred_username", "leser"))
                        .authorities(new SimpleGrantedAuthority("ROLE_leser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("u-123"))
                .andExpect(jsonPath("$.benutzername").value("leser"))
                .andExpect(jsonPath("$.rollen[0]").value("ROLE_leser"));
    }

    @Test
    void adminEndpointVerweigertNichtAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/admin").with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_leser"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointErlaubtAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/admin").with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void offeneEndpunkteBleibenOhneTokenErreichbar() throws Exception {
        // Bestehende Endpunkte sind im PoC-Gate noch nicht abgesichert.
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }
}

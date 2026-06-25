package com.bwa.controlling.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit-Test des Rollen-Mappings realm_access.roles -> ROLE_*. */
class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    private Jwt jwtMit(Object realmAccess) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "none")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .subject("u1");
        if (realmAccess != null) {
            b.claim("realm_access", realmAccess);
        } else {
            b.claim("scope", "openid");
        }
        return b.build();
    }

    @Test
    void mapptRealmRollenAufAuthorities() {
        var authorities = converter.convert(jwtMit(Map.of("roles", List.of("admin", "leser"))));
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_admin", "ROLE_leser");
    }

    @Test
    void leereAuthoritiesOhneRealmAccess() {
        assertThat(converter.convert(jwtMit(null))).isEmpty();
    }
}

package com.bwa.controlling.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource-Server-Konfiguration mit rollenbasierter Autorisierung.
 *
 * Rollenmodell:
 *  - leser:      darf alle Auswertungen/Stammdaten lesen
 *  - bearbeiter: zusätzlich Daten importieren
 *  - admin:      zusätzlich Einstellungen ändern und Adminbereiche
 *
 * (Bewusst keine Rollenhierarchie-Magie: die erlaubten Rollen sind je Endpunkt explizit gelistet.)
 */
@Configuration
public class SecurityConfig {

    private static final String LESER = "leser";
    private static final String BEARBEITER = "bearbeiter";
    private static final String ADMIN = "admin";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Öffentlich: Health/Monitoring
                        .requestMatchers("/api/health", "/actuator/health", "/actuator/info").permitAll()
                        // Auth-Infos
                        .requestMatchers("/api/auth/admin").hasRole(ADMIN)
                        .requestMatchers("/api/auth/**").authenticated()
                        // Admin-Bereiche
                        .requestMatchers("/api/benutzer", "/api/benutzer/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/festschreibung").hasRole(ADMIN)
                        .requestMatchers("/api/gobd-export").hasRole(ADMIN)
                        // Schreiboperationen
                        .requestMatchers(HttpMethod.POST, "/api/import", "/api/import/**").hasAnyRole(BEARBEITER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/einstellungen").hasRole(ADMIN)
                        // Alles übrige unter /api: mindestens Leser
                        .requestMatchers("/api/**").hasAnyRole(LESER, BEARBEITER, ADMIN)
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        return http.build();
    }
}

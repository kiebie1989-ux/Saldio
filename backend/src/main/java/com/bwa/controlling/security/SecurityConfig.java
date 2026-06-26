package com.bwa.controlling.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

    /** Erlaubte CORS-Origins (Frontend). In Produktion auf die echte App-Domain setzen. */
    @Value("${bwa.cors.allowed-origins:http://localhost:4200,http://localhost:14200}")
    private List<String> allowedOrigins;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(f -> f.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)))
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

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }
}

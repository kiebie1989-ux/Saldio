package com.bwa.controlling.revision;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Liefert den aktuell handelnden Benutzer (aus dem JWT) für Audit/Created-by. */
@Component
public class SicherheitsKontext {

    /** Keycloak-sub des aktuellen Benutzers, oder "system" bei technischen/anonymen Aufrufen. */
    public String sub() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getSubject();
        }
        return "system";
    }

    public String benutzername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            Jwt token = jwt.getToken();
            String name = token.getClaimAsString("preferred_username");
            return name != null ? name : token.getSubject();
        }
        return "system";
    }
}

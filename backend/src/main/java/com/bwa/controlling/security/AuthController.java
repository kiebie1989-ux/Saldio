package com.bwa.controlling.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Auth-Endpunkte für das PoC-Gate: bestätigt Tokenvalidierung und Rollenauswertung. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/whoami")
    public BenutzerInfo whoami(@AuthenticationPrincipal Jwt jwt,
                               org.springframework.security.core.Authentication auth) {
        List<String> rollen = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return new BenutzerInfo(jwt.getSubject(), jwt.getClaimAsString("preferred_username"), rollen);
    }

    @GetMapping("/admin")
    public String adminBereich() {
        return "Adminbereich – Zugriff erlaubt";
    }

    public record BenutzerInfo(String subject, String benutzername, List<String> rollen) {}
}

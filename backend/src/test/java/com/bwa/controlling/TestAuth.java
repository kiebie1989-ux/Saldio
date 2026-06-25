package com.bwa.controlling;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

/** Test-Hilfen: Mock-JWT mit den fachlichen Rollen für MockMvc-Requests. */
public final class TestAuth {

    private TestAuth() {}

    public static JwtRequestPostProcessor leser() {
        return rolle("leser");
    }

    public static JwtRequestPostProcessor bearbeiter() {
        return rolle("bearbeiter");
    }

    public static JwtRequestPostProcessor admin() {
        return rolle("admin");
    }

    private static JwtRequestPostProcessor rolle(String rolle) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_" + rolle));
    }
}

package com.bwa.controlling.benutzer;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifiziert die Mandanten-Datentrennung je Benutzer (am sub-Claim). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MandantenZugriffIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;
    @Autowired ImportService importService;
    @Autowired BenutzerRepository benutzerRepository;

    @BeforeEach
    void setup() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
        // Leser "u-leser" darf nur Mustermann GmbH sehen
        Benutzer b = new Benutzer("u-leser", "leser");
        b.setMandanten(new HashSet<>(Set.of("Mustermann GmbH")));
        benutzerRepository.save(b);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor leserMit(String sub) {
        return jwt().jwt(j -> j.subject(sub).claim("preferred_username", sub))
                .authorities(new SimpleGrantedAuthority("ROLE_leser"));
    }

    @Test
    void leserSiehtNurZugewieseneMandanten() throws Exception {
        mockMvc.perform(get("/api/guv").param("mandant", "Mustermann GmbH").param("jahr", "2025")
                        .with(leserMit("u-leser")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/guv").param("mandant", "Beispiel Handel GmbH").param("jahr", "2025")
                        .with(leserMit("u-leser")))
                .andExpect(status().isForbidden());
    }

    @Test
    void mandantenlisteIstGefiltert() throws Exception {
        mockMvc.perform(get("/api/mandanten").with(leserMit("u-leser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mustermann GmbH"));
    }

    @Test
    void adminSiehtAlleMandanten() throws Exception {
        mockMvc.perform(get("/api/guv").param("mandant", "Beispiel Handel GmbH").param("jahr", "2025")
                        .with(TestAuth.admin()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/mandanten").with(TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void adminKannMandantenZuweisenUndZugriffWirktSofort() throws Exception {
        benutzerRepository.save(new Benutzer("u-bearb", "bearb"));

        // ohne Zuweisung: kein Zugriff
        mockMvc.perform(get("/api/guv").param("mandant", "Beispiel Handel GmbH").param("jahr", "2025")
                        .with(leserMit("u-bearb")))
                .andExpect(status().isForbidden());

        // Admin weist Beispiel Handel zu
        mockMvc.perform(put("/api/benutzer").with(TestAuth.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sub\":\"u-bearb\",\"alleMandanten\":false,\"mandanten\":[\"Beispiel Handel GmbH\"]}"))
                .andExpect(status().isOk());

        // jetzt erlaubt
        mockMvc.perform(get("/api/guv").param("mandant", "Beispiel Handel GmbH").param("jahr", "2025")
                        .with(leserMit("u-bearb")))
                .andExpect(status().isOk());
    }

    @Test
    void benutzerverwaltungNurFuerAdmin() throws Exception {
        mockMvc.perform(get("/api/benutzer").with(leserMit("u-leser")))
                .andExpect(status().isForbidden());
    }
}

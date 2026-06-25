package com.bwa.controlling.security;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sichert die rollenbasierte Zugriffs-Matrix ab (wer darf was). */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;

    @Test
    void leseEndpunktBrauchtMindestensLeser() throws Exception {
        // ohne Token -> 401
        mockMvc.perform(get("/api/guv").param("mandant", "Mustermann GmbH").param("jahr", "2025"))
                .andExpect(status().isUnauthorized());
        // mit Admin (Rolle erfüllt + sieht alle Mandanten) -> erlaubt
        mockMvc.perform(get("/api/guv").param("mandant", "Mustermann GmbH").param("jahr", "2025")
                        .with(TestAuth.admin()))
                .andExpect(status().isOk());
    }

    @Test
    void importBrauchtBearbeiter() throws Exception {
        // Leser darf nicht importieren -> 403 (vor Controller abgewiesen)
        mockMvc.perform(multipart("/api/import").param("typ", "csv").with(TestAuth.leser()))
                .andExpect(status().isForbidden());
    }

    @Test
    void einstellungAendernBrauchtAdmin() throws Exception {
        mockMvc.perform(put("/api/einstellungen").with(TestAuth.bearbeiter())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schluessel\":\"Ziel-EBIT-Marge %\",\"wert\":\"30\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthBleibtOeffentlich() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }
}

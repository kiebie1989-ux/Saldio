package com.bwa.controlling.stammdaten;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sichert die abgeleiteten Mitarbeiter-Finanzwerte (€/Std, AG-Anteil, Gesamtkosten) ab. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StammdatenControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;

    @Test
    void mitarbeiterEndpointBerechnetAbgeleiteteWerte() throws Exception {
        // Anna Schmidt: 5200 € / 160 Std = 32,50 €/Std; AG-Anteil 20% = 1040,00; Gesamt 6240,00
        // admin sieht alle Mandanten (Mitarbeiter werden nach erlaubten Mandanten gefiltert)
        mockMvc.perform(get("/api/mitarbeiter").with(com.bwa.controlling.TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personalnummer").value("E001"))
                .andExpect(jsonPath("$[0].euroProStunde").value(32.50))
                .andExpect(jsonPath("$[0].agAnteil").value(1040.00))
                .andExpect(jsonPath("$[0].gesamtkosten").value(6240.00));
    }

    @Test
    void kontenrahmenEndpointLiefertAlleKonten() throws Exception {
        mockMvc.perform(get("/api/kontenrahmen").with(com.bwa.controlling.TestAuth.leser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(84));
    }

    @Test
    void einstellungKannAktualisiertWerden() throws Exception {
        mockMvc.perform(put("/api/einstellungen")
                        .with(com.bwa.controlling.TestAuth.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schluessel\":\"Ziel-EBIT-Marge %\",\"wert\":\"25\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wert").value("25"));

        // jsonPath nutzt intern String.format -> '%' im Schlüssel als '%%' escapen
        mockMvc.perform(get("/api/einstellungen").with(com.bwa.controlling.TestAuth.leser()))
                .andExpect(jsonPath("$[?(@.schluessel=='Ziel-EBIT-Marge %%')].wert").value("25"));
    }
}

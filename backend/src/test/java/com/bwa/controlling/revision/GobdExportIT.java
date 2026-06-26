package com.bwa.controlling.revision;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GoBD-Export: enthält Buchungen, Festschreibungs-Hashkette und Integritätsnachweis (Admin). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GobdExportIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired MockMvc mockMvc;
    @Autowired ImportService importService;
    @Autowired FestschreibungService festschreibung;

    @BeforeEach
    void setup() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
        festschreibung.festschreibe(MANDANT, "2025-01");
    }

    @Test
    void exportLiefertBuchungenFestschreibungUndIntegritaet() throws Exception {
        mockMvc.perform(get("/api/gobd-export").param("mandant", MANDANT).param("jahr", "2025")
                        .with(TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mandant").value(MANDANT))
                .andExpect(jsonPath("$.buchungen.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.festschreibungen[0].monat").value("2025-01"))
                .andExpect(jsonPath("$.festschreibungen[0].periodenHash").isNotEmpty())
                .andExpect(jsonPath("$.integritaet.unveraendert").value(true));
    }

    @Test
    void exportNurFuerAdmin() throws Exception {
        mockMvc.perform(get("/api/gobd-export").param("mandant", MANDANT).param("jahr", "2025")
                        .with(TestAuth.leser()))
                .andExpect(status().isForbidden());
    }
}

package com.bwa.controlling.engine;

import com.bwa.controlling.AbstractPostgresIT;
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

/** Sichert den HTTP-Vertrag von GET /api/guv (JSON-Serialisierung der GuV-Struktur). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EngineControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;
    @Autowired ImportService importService;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void guvEndpointLiefertStrukturierteWerte() throws Exception {
        mockMvc.perform(get("/api/guv").param("mandant", "Mustermann GmbH").param("jahr", "2025")
                        .with(com.bwa.controlling.TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mandant").value("Mustermann GmbH"))
                .andExpect(jsonPath("$.monate.length()").value(12))
                .andExpect(jsonPath("$.zeilen[?(@.position=='Umsatzerlöse')].monate['2025-01']").value(92300.0))
                .andExpect(jsonPath("$.zeilen[?(@.position=='= Rohertrag (DB I)')].ytd").value(820960.0));
    }
}

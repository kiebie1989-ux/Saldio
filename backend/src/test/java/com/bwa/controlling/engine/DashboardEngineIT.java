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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifiziert das Dashboard-Aggregat und den HTTP-Vertrag von /api/dashboard. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardEngineIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired DashboardService dashboardService;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void dashboardLiefertKpisUndMonatsreihe() {
        DashboardBericht d = dashboardService.berechne(MANDANT, 2025);

        assertThat(d.kpis().umsatzYtd()).isEqualByComparingTo("1239460");
        assertThat(d.kpis().rohertragYtd()).isEqualByComparingTo("820960");
        assertThat(d.kpis().ebitYtd()).isEqualByComparingTo("266611");
        assertThat(d.kpis().ebitMarge()).isEqualByComparingTo("21.5");
        assertThat(d.kpis().mitarbeiter()).isEqualTo(4);

        assertThat(d.monatsreihe()).hasSize(12);
        assertThat(d.monatsreihe().get(0).monat()).isEqualTo("2025-01");
        assertThat(d.monatsreihe().get(0).umsatz()).isEqualByComparingTo("92300");
    }

    @Test
    void dashboardHttpVertrag() throws Exception {
        mockMvc.perform(get("/api/dashboard").param("mandant", MANDANT).param("jahr", "2025")
                        .with(com.bwa.controlling.TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.mitarbeiter").value(4))
                .andExpect(jsonPath("$.monatsreihe.length()").value(12));
    }
}

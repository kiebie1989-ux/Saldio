package com.bwa.controlling.stammdaten;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** E1: Mandanten-CRUD (Admin) und Jahre-Endpoint. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MandantCrudIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;
    @Autowired ImportService importService;

    @Test
    void adminLegtMandantMitDatevNummerAn() throws Exception {
        mockMvc.perform(post("/api/mandanten").with(TestAuth.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neu GmbH\",\"status\":\"AKTIV\",\"imEinzelbericht\":true,"
                                + "\"inKumulierung\":true,\"imFinalbericht\":false,\"typ\":\"GmbH\","
                                + "\"bemerkung\":\"Test\",\"datevMandantennr\":\"55555\",\"datevBeraternr\":\"1001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neu GmbH"))
                .andExpect(jsonPath("$.datevMandantennr").value("55555"));
    }

    @Test
    void anlegenNurFuerAdmin() throws Exception {
        mockMvc.perform(post("/api/mandanten").with(TestAuth.bearbeiter())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void jahreEndpointLiefertVerfuegbareJahre() throws Exception {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));

        mockMvc.perform(get("/api/jahre").param("mandant", "Mustermann GmbH").with(TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2025));
    }
}

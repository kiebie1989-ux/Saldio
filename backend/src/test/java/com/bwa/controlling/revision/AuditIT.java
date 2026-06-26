package com.bwa.controlling.revision;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prüft, dass mutierende Operationen lückenlos im Audit-Trail landen. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;
    @Autowired AuditService audit;

    @Test
    void importWirdMitBenutzerAuditiert() throws Exception {
        byte[] inhalt = getClass().getResourceAsStream("/samples/bwa_simple.csv").readAllBytes();
        mockMvc.perform(multipart("/api/import")
                        .file(new MockMultipartFile("datei", "bwa_simple.csv", "text/csv", inhalt))
                        .param("typ", "csv").with(TestAuth.bearbeiter()))
                .andExpect(status().isOk());

        assertThat(audit.alle()).anySatisfy(a -> {
            assertThat(a.getAktion()).isEqualTo("IMPORT");
            assertThat(a.getEntitaet()).isEqualTo("import_batch");
            assertThat(a.getBenutzerSub()).isNotBlank();
        });
    }

    @Test
    void einstellungsaenderungWirdAuditiert() throws Exception {
        mockMvc.perform(put("/api/einstellungen").with(TestAuth.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schluessel\":\"Ziel-EBIT-Marge %\",\"wert\":\"19\"}"))
                .andExpect(status().isOk());

        assertThat(audit.alle()).anySatisfy(a ->
                assertThat(a.getAktion()).isEqualTo("EINSTELLUNG_AENDERN"));
    }
}

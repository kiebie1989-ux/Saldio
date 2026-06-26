package com.bwa.controlling.imports;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.TestAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Beweist den Upload-Endpoint POST /api/import (Multipart) end-to-end. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImportControllerIT extends AbstractPostgresIT {

    @Autowired MockMvc mockMvc;

    @Test
    void csvUploadLiefertImportZusammenfassung() throws Exception {
        byte[] content = getClass().getResourceAsStream("/samples/bwa_simple.csv").readAllBytes();
        MockMultipartFile file = new MockMultipartFile(
                "datei", "bwa_simple.csv", "text/csv", content);

        mockMvc.perform(multipart("/api/import").file(file).param("typ", "csv").with(TestAuth.bearbeiter()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zeilenGesamt").value(6))
                .andExpect(jsonPath("$.zeilenOk").value(6))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void ungueltigerTypErgibt400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("datei", "x.txt", "text/plain", new byte[] {1});
        mockMvc.perform(multipart("/api/import").file(file).param("typ", "xml").with(TestAuth.bearbeiter()))
                .andExpect(status().isBadRequest());
    }
}

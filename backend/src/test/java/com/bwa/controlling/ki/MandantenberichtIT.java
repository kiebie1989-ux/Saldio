package com.bwa.controlling.ki;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrationstest des Mandantenberichts. Standard ist provider=none -> regelbasiert,
 * also voll deterministisch ohne LLM-Infrastruktur. Prüft auch die PDF-Erzeugung.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MandantenberichtIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired MandantenberichtService berichtService;
    @Autowired PdfService pdfService;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void berichtIstRegelbasiertUndVollstaendig() {
        Mandantenbericht b = berichtService.erzeuge(MANDANT, 2025);

        assertThat(b.quelle()).isEqualTo("regelbasiert");
        assertThat(b.bereiche()).hasSize(6);
        assertThat(b.managementkommentar()).hasSize(6);
        // Sonstigenquote ist rot -> Kostenabschnitt meldet es kritisch
        assertThat(b.managementkommentar().get(2).text()).contains("KRITISCH");
    }

    @Test
    void pdfWirdErzeugt() {
        byte[] pdf = pdfService.alsPdf(berichtService.erzeuge(MANDANT, 2025));
        assertThat(pdf).isNotEmpty();
        // PDF-Magic-Bytes "%PDF"
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void httpEndpunkteLiefernJsonUndPdf() throws Exception {
        mockMvc.perform(get("/api/mandantenbericht").param("mandant", MANDANT).param("jahr", "2025")
                        .with(com.bwa.controlling.TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quelle").value("regelbasiert"))
                .andExpect(jsonPath("$.bereiche.length()").value(6));

        byte[] pdf = mockMvc.perform(get("/api/mandantenbericht/pdf")
                        .param("mandant", MANDANT).param("jahr", "2025")
                        .with(com.bwa.controlling.TestAuth.admin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}

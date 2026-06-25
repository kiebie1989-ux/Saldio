package com.bwa.controlling.engine;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiziert die GuV-Engine gegen die von der Excel berechneten Werte (Blatt GuV_Struktur).
 * Datenbasis: die vollständige Saldenliste 2025 (122 Zeilen) als Fixture.
 */
@SpringBootTest
@Transactional
class GuvEngineIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired GuvService guvService;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void guvReproduziertExcelGoldenValues() {
        GuvBericht b = guvService.berechne(MANDANT, 2025);

        // --- Januar 2025 ---
        assertThat(monat(b, "Umsatzerlöse", "2025-01")).isEqualByComparingTo("92300");
        assertThat(monat(b, "= Gesamtleistung", "2025-01")).isEqualByComparingTo("92300");
        assertThat(monat(b, "- Wareneinsatz / Materialeinsatz", "2025-01")).isEqualByComparingTo("31000");
        assertThat(monat(b, "= Rohertrag (DB I)", "2025-01")).isEqualByComparingTo("61300");
        assertThat(monat(b, "= EBITDA", "2025-01")).isEqualByComparingTo("22030");
        assertThat(monat(b, "= EBIT (Betriebsergebnis)", "2025-01")).isEqualByComparingTo("22030");
        assertThat(monat(b, "= Jahresüberschuss / -fehlbetrag", "2025-01")).isEqualByComparingTo("22030");

        // --- September 2025 ---
        assertThat(monat(b, "= Rohertrag (DB I)", "2025-09")).isEqualByComparingTo("71070");
        assertThat(monat(b, "= EBITDA", "2025-09")).isEqualByComparingTo("22110");

        // --- YTD (Summe aller Monate) ---
        assertThat(ytd(b, "Umsatzerlöse")).isEqualByComparingTo("1239460");
        assertThat(ytd(b, "- Wareneinsatz / Materialeinsatz")).isEqualByComparingTo("418500");
        assertThat(ytd(b, "= Rohertrag (DB I)")).isEqualByComparingTo("820960");
        assertThat(ytd(b, "- Personalaufwand")).isEqualByComparingTo("275500");
        assertThat(ytd(b, "= EBITDA")).isEqualByComparingTo("266611");
        assertThat(ytd(b, "= Jahresüberschuss / -fehlbetrag")).isEqualByComparingTo("266611");
    }

    @Test
    void aggregationFiltertNachMandant() {
        // Beispiel Handel GmbH ist ebenfalls im Fixture -> muss separat und ungleich Mustermann sein.
        GuvBericht handel = guvService.berechne("Beispiel Handel GmbH", 2025);
        assertThat(ytd(handel, "Umsatzerlöse")).isGreaterThan(BigDecimal.ZERO);
        assertThat(ytd(handel, "Umsatzerlöse")).isNotEqualByComparingTo("1239460");
    }

    private static BigDecimal monat(GuvBericht b, String pos, String monat) {
        return zeile(b, pos).monate().get(monat);
    }

    private static BigDecimal ytd(GuvBericht b, String pos) {
        return zeile(b, pos).ytd();
    }

    private static GuvBericht.GuvZeile zeile(GuvBericht b, String pos) {
        return b.zeilen().stream().filter(z -> z.position().equals(pos)).findFirst()
                .orElseThrow(() -> new AssertionError("GuV-Position fehlt: " + pos));
    }
}

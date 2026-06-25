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
 * Verifiziert die Bilanz-Engine. Die Saldenliste enthält nur einen Teil der Bilanzgruppen
 * (Forderungen, Bankguthaben, Fremdkapital) — die Leaf-Aggregationen werden gegen die
 * Rohdaten-Summen geprüft (unabhängig von den Doppelzählungs-Bugs des Excel-Originals).
 */
@SpringBootTest
@Transactional
class BilanzEngineIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired BilanzService bilanzService;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void bilanzLeafsEntsprechenRohdatenSummen() {
        BilanzBericht b = bilanzService.berechne(MANDANT, 2025);

        assertThat(ytd(b, "2. Forderungen aus LuL")).isEqualByComparingTo("599500");
        assertThat(ytd(b, "3. Bankguthaben")).isEqualByComparingTo("353500");
        assertThat(ytd(b, "II. Verbindlichkeiten (LuL + Darlehen)")).isEqualByComparingTo("249600");

        // Umlaufvermögen = Summe der Aktiva-Leafs (nur Forderungen + Bank vorhanden)
        assertThat(ytd(b, "= Umlaufvermögen")).isEqualByComparingTo("953000");
        assertThat(ytd(b, "= BILANZSUMME AKTIVA")).isEqualByComparingTo("953000");
    }

    @Test
    void bilanzdifferenzWirdBerechnet() {
        BilanzBericht b = bilanzService.berechne(MANDANT, 2025);
        // Aktiva (953000) - Passiva (Fremdkapital 249600) ; Daten sind unvollständig -> Differenz != 0.
        assertThat(ytd(b, "Bilanzdifferenz (Soll = 0)"))
                .isEqualByComparingTo(ytd(b, "= BILANZSUMME AKTIVA").subtract(ytd(b, "= BILANZSUMME PASSIVA")));
    }

    private static BigDecimal ytd(BilanzBericht b, String pos) {
        return b.zeilen().stream().filter(z -> z.position().equals(pos)).findFirst()
                .orElseThrow(() -> new AssertionError("Bilanz-Position fehlt: " + pos)).ytd();
    }
}

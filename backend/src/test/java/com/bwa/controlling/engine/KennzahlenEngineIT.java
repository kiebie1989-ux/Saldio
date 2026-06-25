package com.bwa.controlling.engine;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.engine.Kennzahl.Ampel;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiziert die Kennzahlen + Ampellogik. Goldenwerte sind deterministisch aus den
 * P3-verifizierten YTD-Zahlen abgeleitet (die Excel-Kennzahlen selbst waren Konstanten).
 */
@SpringBootTest
@Transactional
class KennzahlenEngineIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired KennzahlenService kennzahlenService;

    @BeforeEach
    void importiereSaldenliste() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void ratiosWerdenKorrektBerechnet() {
        List<Kennzahl> k = kennzahlenService.berechne(MANDANT, 2025);

        assertThat(wert(k, "Rohertragsquote")).isEqualByComparingTo("66.2");
        assertThat(wert(k, "EBIT-Marge")).isEqualByComparingTo("21.5");
        assertThat(wert(k, "Personalquote")).isEqualByComparingTo("22.2");
        assertThat(wert(k, "Materialquote")).isEqualByComparingTo("33.8");
        assertThat(wert(k, "Sonstigenquote")).isEqualByComparingTo("22.5");
        assertThat(wert(k, "Liquidität 1. Grades")).isEqualByComparingTo("141.6");
        assertThat(wert(k, "Liquidität 2. Grades")).isEqualByComparingTo("381.8");
        assertThat(wert(k, "Deckungsbeitrag DB I")).isEqualByComparingTo("820960.00");
        assertThat(wert(k, "Umsatz je Mitarbeiter")).isEqualByComparingTo("309865.00");
        assertThat(wert(k, "EBIT je Mitarbeiter")).isEqualByComparingTo("66652.75");
    }

    @Test
    void ampelLogikGreiftGegenZielwerte() {
        List<Kennzahl> k = kennzahlenService.berechne(MANDANT, 2025);

        // EBIT-Marge 21,5 >= Ziel 18 (höher besser) -> grün
        assertThat(kennzahl(k, "EBIT-Marge").ampel()).isEqualTo(Ampel.GRUEN);
        // Materialquote 33,8 <= Ziel 35 (niedriger besser) -> grün
        assertThat(kennzahl(k, "Materialquote").ampel()).isEqualTo(Ampel.GRUEN);
        // Sonstigenquote 22,5 > Ziel 20 und > 20*1,1=22 (niedriger besser) -> rot (wie im Excel 🔴)
        assertThat(kennzahl(k, "Sonstigenquote").ampel()).isEqualTo(Ampel.ROT);
        // DB I ohne Zielwert -> neutral
        assertThat(kennzahl(k, "Deckungsbeitrag DB I").ampel()).isEqualTo(Ampel.NEUTRAL);
    }

    private static BigDecimal wert(List<Kennzahl> k, String name) {
        return kennzahl(k, name).wert();
    }

    private static Kennzahl kennzahl(List<Kennzahl> k, String name) {
        return k.stream().filter(x -> x.name().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Kennzahl fehlt: " + name));
    }
}

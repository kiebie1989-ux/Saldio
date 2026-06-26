package com.bwa.controlling.imports;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Vorzeichenmatrix S/H × Kontenklasse (BWA-Konvention, inkl. Storno). */
class BetragsLogikTest {

    private static final BigDecimal H = new BigDecimal("100.00");

    @Test
    void erloeseHabenPositivSollNegativ() {
        // Erlöse erhöhen sich im Haben -> H = +, S = - (Storno)
        assertThat(BetragsLogik.vorzeichenrichtig(H, "H", "Erlöse")).isEqualByComparingTo("100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "S", "Erlöse")).isEqualByComparingTo("-100.00");
    }

    @Test
    void aufwandSollPositivHabenNegativ() {
        // Aufwand erhöht sich im Soll -> S = +, H = - (Storno)
        assertThat(BetragsLogik.vorzeichenrichtig(H, "S", "Aufwand")).isEqualByComparingTo("100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "H", "Aufwand")).isEqualByComparingTo("-100.00");
    }

    @Test
    void aktivaSollPositivPassivaHabenPositiv() {
        assertThat(BetragsLogik.vorzeichenrichtig(H, "S", "Aktiva")).isEqualByComparingTo("100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "H", "Aktiva")).isEqualByComparingTo("-100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "H", "Passiva")).isEqualByComparingTo("100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "S", "Passiva")).isEqualByComparingTo("-100.00");
    }

    @Test
    void ohneSollHabenBleibtBetragUnveraendert() {
        // CSV-Saldenliste liefert bereits den Netto-Betrag (kein S/H)
        assertThat(BetragsLogik.vorzeichenrichtig(H, null, "Aufwand")).isEqualByComparingTo("100.00");
        assertThat(BetragsLogik.vorzeichenrichtig(H, "", "Erlöse")).isEqualByComparingTo("100.00");
    }
}

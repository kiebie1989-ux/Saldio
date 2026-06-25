package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Ergebnis der Bilanz-Berechnung (Excel-Blatt Bilanz_Struktur), korrekt modelliert
 * (ohne die Doppel-/Dreifachzählung des Originals). Werte je Monat sind Periodensalden.
 */
public record BilanzBericht(String mandant, int jahr, List<String> monate, List<BilanzZeile> zeilen) {

    /** YTD-Wert einer Position (0, falls nicht vorhanden). */
    public BigDecimal ytd(String position) {
        return zeilen.stream().filter(z -> z.position().equals(position)).findFirst()
                .map(BilanzZeile::ytd).orElse(BigDecimal.ZERO);
    }

    public record BilanzZeile(String position, Art art, Map<String, BigDecimal> monate, BigDecimal ytd) {
        public enum Art { WERT, SUMME, PRUEFUNG }
    }
}

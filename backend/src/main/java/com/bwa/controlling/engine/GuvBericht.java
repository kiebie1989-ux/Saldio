package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Ergebnis der GuV-Berechnung (Excel-Blatt GuV_Struktur).
 *
 * @param mandant Mandantenname
 * @param jahr    Geschäftsjahr
 * @param monate  geordnete Monatsschlüssel (JJJJ-MM)
 * @param zeilen  GuV-Positionen in Darstellungsreihenfolge
 */
public record GuvBericht(String mandant, int jahr, List<String> monate, List<GuvZeile> zeilen) {

    /** YTD-Wert einer Position (0, falls nicht vorhanden). */
    public BigDecimal ytd(String position) {
        return zeilen.stream().filter(z -> z.position().equals(position)).findFirst()
                .map(GuvZeile::ytd).orElse(BigDecimal.ZERO);
    }

    /** Monatswert einer Position (0, falls nicht vorhanden). */
    public BigDecimal monat(String position, String monat) {
        return zeilen.stream().filter(z -> z.position().equals(position)).findFirst()
                .map(z -> z.monate().getOrDefault(monat, BigDecimal.ZERO)).orElse(BigDecimal.ZERO);
    }

    /**
     * @param position Bezeichnung der GuV-Position
     * @param art      WERT (aggregierter Gruppenwert) oder SUMME (berechnete Zwischensumme)
     * @param monate   Monatswert je Monatsschlüssel
     * @param ytd      Summe über alle Monate des Jahres
     */
    public record GuvZeile(String position, Art art, Map<String, BigDecimal> monate, BigDecimal ytd) {
        public enum Art { WERT, SUMME }
    }
}

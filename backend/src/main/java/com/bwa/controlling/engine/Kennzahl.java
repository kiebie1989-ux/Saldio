package com.bwa.controlling.engine;

import java.math.BigDecimal;

/**
 * Eine berechnete Kennzahl mit Ampelbewertung (Excel-Blatt 05_Kennzahlen).
 *
 * @param name           Bezeichnung
 * @param wert           berechneter Wert
 * @param einheit        "%", "€" oder "€/MA"
 * @param zielwert       Schwellwert für die Ampel (null = keine Bewertung)
 * @param ampel          GRUEN/GELB/ROT/NEUTRAL
 * @param richtung       ob höhere oder niedrigere Werte besser sind
 * @param interpretation kurze fachliche Einordnung
 */
public record Kennzahl(
        String name,
        BigDecimal wert,
        String einheit,
        BigDecimal zielwert,
        Ampel ampel,
        Richtung richtung,
        String interpretation
) {
    public enum Ampel { GRUEN, GELB, ROT, NEUTRAL }

    public enum Richtung { HOEHER_BESSER, NIEDRIGER_BESSER, KEINE }
}

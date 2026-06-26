package com.bwa.controlling.imports;

import java.math.BigDecimal;

/**
 * Vorzeichenlogik für Buchungsbeträge in der BWA-Sicht.
 *
 * DATEV-EXTF liefert den Umsatz stets als positiven Betrag plus ein Soll/Haben-Kennzeichen.
 * Für die BWA muss daraus ein vorzeichenrichtiger Betrag je Konto-/BWA-Gruppe werden, sodass die
 * Gruppensummen die übliche Konvention haben: Erlöse/Erträge/Aktiva/Passiva auf ihrer normalen
 * (erhöhenden) Buchungsseite positiv, Gegenbuchungen (Storno) negativ. Die GuV/Bilanz-Struktur
 * subtrahiert die Aufwandsgruppen anschließend selbst.
 *
 * Normale (erhöhende) Buchungsseite je Kontenklasse:
 *   Erlöse / Ertrag / Passiva  -> Haben (H)
 *   Aufwand / Aktiva           -> Soll  (S)
 *
 * Beim vereinfachten CSV-Import gibt es kein S/H — der gelieferte Betrag gilt unverändert.
 */
public final class BetragsLogik {

    private BetragsLogik() {}

    public static BigDecimal vorzeichenrichtig(BigDecimal umsatz, String sollHaben, String kontenklasse) {
        if (umsatz == null) {
            return BigDecimal.ZERO;
        }
        if (sollHaben == null || sollHaben.isBlank()) {
            return umsatz; // kein S/H (z.B. CSV-Saldenliste) -> Betrag wie geliefert
        }
        boolean haben = sollHaben.trim().toUpperCase().startsWith("H");
        boolean normaleSeite = (istHabenKonto(kontenklasse) == haben);
        return normaleSeite ? umsatz : umsatz.negate();
    }

    /** Konten, die auf der Haben-Seite zunehmen (Erlöse, Erträge, Passiva). */
    static boolean istHabenKonto(String kontenklasse) {
        if (kontenklasse == null) {
            return true; // konservativ: wie Ertrag behandeln
        }
        return switch (kontenklasse.trim()) {
            case "Erlöse", "Ertrag", "Passiva" -> true;
            default -> false; // Aufwand, Aktiva -> Soll-Konto
        };
    }
}

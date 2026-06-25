package com.bwa.controlling.ki;

import com.bwa.controlling.engine.Kennzahl.Ampel;

import java.util.List;

/**
 * Executive-Mandantenbericht (Excel-Blätter 06_Mandantenbericht & 07_KI_Auswertung):
 * Bereichsanalysen mit Ampel + ein mehrteiliger Managementkommentar.
 *
 * @param quelle Herkunft der Texte: "regelbasiert" oder "KI: &lt;provider&gt;"
 */
public record Mandantenbericht(
        String mandant,
        int jahr,
        String quelle,
        List<BereichsAnalyse> bereiche,
        List<Abschnitt> managementkommentar
) {
    /** Tiefenanalyse je Bereich (07_KI_Auswertung). */
    public record BereichsAnalyse(String bereich, Ampel ampel, String bewertung, String massnahme, String analyse) {}

    /** Ein Abschnitt des Managementkommentars (06_Mandantenbericht). */
    public record Abschnitt(String titel, String text) {}
}

package com.bwa.controlling.ki;

/** Erzeugt aus den Berichtsfakten die Bereichsanalysen und den Managementkommentar. */
public interface KommentarGenerator {

    /** Kennung der Textquelle, z.B. "regelbasiert" oder "KI: ollama". */
    String quelle();

    Mandantenbericht erzeuge(BerichtsFakten fakten);
}

package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;

/** Mandantenübergreifende Sicht (Excel-Blatt 08_Kumuliert_Final): Einzeln / Kumuliert / Final. */
public record KumuliertBericht(String modus, int jahr, List<MandantKennzahl> mandanten, MandantKennzahl summe) {

    public record MandantKennzahl(
            String mandant,
            BigDecimal umsatz,
            BigDecimal rohertrag,
            BigDecimal rohertragsquote,
            BigDecimal ebit,
            BigDecimal ebitMarge) {}
}

package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;

/** Kostenstruktur (Excel-Blatt 10_Kostenstruktur): Kostenarten je Monat + Kostenstellen-Analyse. */
public record KostenstrukturBericht(
        String mandant,
        int jahr,
        List<KostenartZeile> kostenarten,
        List<KostenstelleZeile> kostenstellen
) {
    public record KostenartZeile(
            String monat,
            BigDecimal umsatz,
            BigDecimal wareneinsatz, BigDecimal weQuote,
            BigDecimal personal, BigDecimal persQuote,
            BigDecimal sonstige, BigDecimal sonsQuote,
            BigDecimal gesamtkosten, BigDecimal gesamtkostenquote) {}

    public record KostenstelleZeile(String kostenstelle, BigDecimal personalkosten, BigDecimal anteil) {}
}

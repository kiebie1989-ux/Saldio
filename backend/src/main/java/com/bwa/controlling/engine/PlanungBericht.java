package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;

/** Planung & Forecast (Excel-Blatt 09_Planung): IST bis Stichmonat + projizierte Planmonate. */
public record PlanungBericht(
        String mandant,
        int jahr,
        int bisMonat,
        String szenario,
        List<PlanZeile> zeilen,
        Jahreswerte jahresprognose
) {
    /** typ = "IST" oder "PLAN". */
    public record PlanZeile(String monat, String typ, BigDecimal umsatz, BigDecimal rohertrag, BigDecimal ebit) {}

    public record Jahreswerte(BigDecimal umsatz, BigDecimal rohertrag, BigDecimal ebit) {}
}

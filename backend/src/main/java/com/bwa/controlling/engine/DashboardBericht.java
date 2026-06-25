package com.bwa.controlling.engine;

import java.math.BigDecimal;
import java.util.List;

/** Aggregierte Dashboard-Sicht: KPI-Kacheln + Monatsreihe (Excel-Blatt 04_Dashboard). */
public record DashboardBericht(String mandant, int jahr, Kpis kpis, List<MonatsWert> monatsreihe) {

    public record Kpis(
            BigDecimal umsatzYtd,
            BigDecimal rohertragYtd,
            BigDecimal ebitYtd,
            BigDecimal ebitMarge,
            long mitarbeiter) {}

    public record MonatsWert(String monat, BigDecimal umsatz, BigDecimal rohertrag, BigDecimal ebit) {}
}

package com.bwa.controlling.ki;

import com.bwa.controlling.engine.DashboardBericht;
import com.bwa.controlling.engine.Kennzahl;

import java.util.List;
import java.util.Optional;

/** Deterministische Faktenbasis eines Berichts: KPIs + Kennzahlen. Grundlage für Regeln und Prompts. */
public record BerichtsFakten(String mandant, int jahr, DashboardBericht.Kpis kpis, List<Kennzahl> kennzahlen) {

    public Optional<Kennzahl> kennzahl(String name) {
        return kennzahlen.stream().filter(k -> k.name().equals(name)).findFirst();
    }
}

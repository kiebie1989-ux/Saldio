package com.bwa.controlling.ki;

import com.bwa.controlling.engine.DashboardBericht;
import com.bwa.controlling.engine.Kennzahl;
import com.bwa.controlling.engine.Kennzahl.Ampel;
import com.bwa.controlling.engine.Kennzahl.Richtung;

import java.math.BigDecimal;
import java.util.List;

/** Gemeinsame Testfakten für die KI-Generatoren (entsprechen den P3/P4-Goldenwerten). */
final class KommentarFixtures {

    private KommentarFixtures() {}

    static BerichtsFakten mustermann() {
        DashboardBericht.Kpis kpis = new DashboardBericht.Kpis(
                new BigDecimal("1239460"), new BigDecimal("820960"),
                new BigDecimal("266611"), new BigDecimal("21.5"), 4);

        List<Kennzahl> kennzahlen = List.of(
                kz("EBIT-Marge", "21.5", "18", Ampel.GRUEN, Richtung.HOEHER_BESSER),
                kz("Rohertragsquote", "66.2", "65", Ampel.GRUEN, Richtung.HOEHER_BESSER),
                kz("Liquidität 1. Grades", "141.6", "120", Ampel.GRUEN, Richtung.HOEHER_BESSER),
                kz("Liquidität 2. Grades", "381.8", "150", Ampel.GRUEN, Richtung.HOEHER_BESSER),
                kz("Rentabilität (RoS)", "21.5", "20", Ampel.GRUEN, Richtung.HOEHER_BESSER),
                kz("Personalquote", "22.2", "25", Ampel.GRUEN, Richtung.NIEDRIGER_BESSER),
                kz("Materialquote", "33.8", "35", Ampel.GRUEN, Richtung.NIEDRIGER_BESSER),
                kz("Sonstigenquote", "22.5", "20", Ampel.ROT, Richtung.NIEDRIGER_BESSER));

        return new BerichtsFakten("Mustermann GmbH", 2025, kpis, kennzahlen);
    }

    private static Kennzahl kz(String name, String wert, String ziel, Ampel ampel, Richtung richtung) {
        return new Kennzahl(name, new BigDecimal(wert), "%", new BigDecimal(ziel), ampel, richtung, name + " Test");
    }
}

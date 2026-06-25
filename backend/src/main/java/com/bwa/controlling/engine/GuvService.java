package com.bwa.controlling.engine;

import com.bwa.controlling.engine.GuvBericht.GuvZeile;
import com.bwa.controlling.engine.GuvBericht.GuvZeile.Art;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Berechnet die Gewinn- und Verlustrechnung (GuV) aus den aggregierten Buchungen.
 * Bildet die Struktur des Excel-Blatts GuV_Struktur ab: die SUMIFS-Aggregation nach BWA-Gruppe
 * liefert PostgreSQL, die Zwischensummen (Gesamtleistung, Rohertrag, EBITDA, EBIT, EBT,
 * Jahresüberschuss) werden hier komponiert.
 */
@Service
public class GuvService {

    /** Operative Aufwandsgruppen zwischen Rohertrag und EBITDA. */
    private static final List<String> BETRIEBSKOSTEN_GRUPPEN = List.of(
            "Personal", "Raumkosten", "Kfz-Kosten", "Marketing", "Reisekosten",
            "Büro & IT", "Versicherungen", "Sonstige");

    private final BuchungAggregatRepository repository;

    public GuvService(BuchungAggregatRepository repository) {
        this.repository = repository;
    }

    public GuvBericht berechne(String mandant, int jahr) {
        // Aggregat aus Postgres -> Map<monat, Map<gruppe, summe>>
        Map<String, Map<String, BigDecimal>> proMonat = new HashMap<>();
        for (GruppenSumme gs : repository.aggregiere(mandant, String.valueOf(jahr))) {
            proMonat.computeIfAbsent(gs.getMonat(), m -> new HashMap<>())
                    .merge(gs.getGruppe() == null ? "" : gs.getGruppe(),
                           gs.getSumme() == null ? BigDecimal.ZERO : gs.getSumme(), BigDecimal::add);
        }

        List<String> monate = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            monate.add("%d-%02d".formatted(jahr, m));
        }

        // Pro Position pro Monat den Wert berechnen.
        Map<String, Map<String, BigDecimal>> werte = new LinkedHashMap<>();
        for (String monat : monate) {
            Map<String, BigDecimal> g = proMonat.getOrDefault(monat, Map.of());

            BigDecimal umsatz = grp(g, "Umsatz");
            BigDecimal sonstigeErloese = grp(g, "Sonstige Erlöse");
            BigDecimal gesamtleistung = umsatz.add(sonstigeErloese);
            BigDecimal wareneinsatz = grp(g, "Wareneinsatz");
            BigDecimal fremdleistungen = grp(g, "Fremdleistungen");
            BigDecimal rohertrag = gesamtleistung.subtract(wareneinsatz).subtract(fremdleistungen);

            BigDecimal betriebskosten = BigDecimal.ZERO;
            for (String k : BETRIEBSKOSTEN_GRUPPEN) {
                betriebskosten = betriebskosten.add(grp(g, k));
            }
            BigDecimal ebitda = rohertrag.subtract(betriebskosten);
            BigDecimal abschreibungen = grp(g, "Abschreibungen");
            BigDecimal ebit = ebitda.subtract(abschreibungen);
            BigDecimal finanzergebnis = grp(g, "Finanzergebnis");
            BigDecimal ebt = ebit.add(finanzergebnis);
            BigDecimal steuern = grp(g, "Steuern");
            BigDecimal jahresueberschuss = ebt.subtract(steuern);

            put(werte, "Umsatzerlöse", monat, umsatz);
            put(werte, "+ Sonstige Erlöse", monat, sonstigeErloese);
            put(werte, "= Gesamtleistung", monat, gesamtleistung);
            put(werte, "- Wareneinsatz / Materialeinsatz", monat, wareneinsatz);
            put(werte, "- Fremdleistungen", monat, fremdleistungen);
            put(werte, "= Rohertrag (DB I)", monat, rohertrag);
            put(werte, "- Personalaufwand", monat, grp(g, "Personal"));
            put(werte, "- Raumkosten", monat, grp(g, "Raumkosten"));
            put(werte, "- Kfz-Kosten", monat, grp(g, "Kfz-Kosten"));
            put(werte, "- Marketing & Werbung", monat, grp(g, "Marketing"));
            put(werte, "- Reisekosten", monat, grp(g, "Reisekosten"));
            put(werte, "- IT & Bürokosten", monat, grp(g, "Büro & IT"));
            put(werte, "- Versicherungen & Beiträge", monat, grp(g, "Versicherungen"));
            put(werte, "- Sonstige Betriebskosten", monat, grp(g, "Sonstige"));
            put(werte, "= EBITDA", monat, ebitda);
            put(werte, "- Abschreibungen (AfA)", monat, abschreibungen);
            put(werte, "= EBIT (Betriebsergebnis)", monat, ebit);
            put(werte, "+/- Finanzergebnis", monat, finanzergebnis);
            put(werte, "= EBT (Ergebnis vor Steuern)", monat, ebt);
            put(werte, "- Steuern", monat, steuern);
            put(werte, "= Jahresüberschuss / -fehlbetrag", monat, jahresueberschuss);
        }

        List<GuvZeile> zeilen = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> e : werte.entrySet()) {
            String pos = e.getKey();
            Art art = pos.startsWith("=") ? Art.SUMME : Art.WERT;
            BigDecimal ytd = e.getValue().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            zeilen.add(new GuvZeile(pos, art, e.getValue(), ytd));
        }
        return new GuvBericht(mandant, jahr, monate, zeilen);
    }

    private static BigDecimal grp(Map<String, BigDecimal> g, String gruppe) {
        return g.getOrDefault(gruppe, BigDecimal.ZERO);
    }

    private static void put(Map<String, Map<String, BigDecimal>> werte, String pos, String monat, BigDecimal v) {
        werte.computeIfAbsent(pos, p -> new LinkedHashMap<>()).put(monat, v);
    }
}

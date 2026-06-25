package com.bwa.controlling.engine;

import com.bwa.controlling.engine.BilanzBericht.BilanzZeile;
import com.bwa.controlling.engine.BilanzBericht.BilanzZeile.Art;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Berechnet die Bilanz aus den aggregierten Buchungen (BWA-Gruppen der Bilanzkonten).
 * Aktiva = Anlagevermögen + Umlaufvermögen; Passiva = Eigenkapital + Verbindlichkeiten +
 * Rückstellungen; plus Bilanzdifferenz-Prüfung (Aktiva - Passiva).
 */
@Service
public class BilanzService {

    private final BuchungAggregatRepository repository;

    public BilanzService(BuchungAggregatRepository repository) {
        this.repository = repository;
    }

    public BilanzBericht berechne(String mandant, int jahr) {
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

        Map<String, Map<String, BigDecimal>> w = new LinkedHashMap<>();
        for (String monat : monate) {
            Map<String, BigDecimal> g = proMonat.getOrDefault(monat, Map.of());

            BigDecimal anlagevermoegen = grp(g, "Anlagevermögen");
            BigDecimal vorraete = grp(g, "Vorräte");
            BigDecimal forderungen = grp(g, "Forderungen");
            BigDecimal bank = grp(g, "Bankguthaben");
            BigDecimal kasse = grp(g, "Kassenbestand");
            BigDecimal vorsteuer = grp(g, "Vorsteuer");
            BigDecimal umlaufvermoegen = vorraete.add(forderungen).add(bank).add(kasse).add(vorsteuer);
            BigDecimal bilanzsummeAktiva = anlagevermoegen.add(umlaufvermoegen);

            BigDecimal eigenkapital = grp(g, "Eigenkapital");
            BigDecimal fremdkapital = grp(g, "Fremdkapital");
            BigDecimal steuervb = grp(g, "Steuervb.");
            BigDecimal verbindlichkeiten = fremdkapital.add(steuervb);
            BigDecimal rueckstellungen = grp(g, "Rückstellungen");
            BigDecimal bilanzsummePassiva = eigenkapital.add(verbindlichkeiten).add(rueckstellungen);

            BigDecimal differenz = bilanzsummeAktiva.subtract(bilanzsummePassiva);

            put(w, "I. Anlagevermögen", monat, anlagevermoegen);
            put(w, "1. Vorräte", monat, vorraete);
            put(w, "2. Forderungen aus LuL", monat, forderungen);
            put(w, "3. Bankguthaben", monat, bank);
            put(w, "4. Kasse", monat, kasse);
            put(w, "5. Vorsteuer", monat, vorsteuer);
            put(w, "= Umlaufvermögen", monat, umlaufvermoegen);
            put(w, "= BILANZSUMME AKTIVA", monat, bilanzsummeAktiva);
            put(w, "I. Eigenkapital", monat, eigenkapital);
            put(w, "II. Verbindlichkeiten (LuL + Darlehen)", monat, fremdkapital);
            put(w, "  Steuerverbindlichkeiten", monat, steuervb);
            put(w, "= Verbindlichkeiten gesamt", monat, verbindlichkeiten);
            put(w, "III. Rückstellungen", monat, rueckstellungen);
            put(w, "= BILANZSUMME PASSIVA", monat, bilanzsummePassiva);
            put(w, "Bilanzdifferenz (Soll = 0)", monat, differenz);
        }

        List<BilanzZeile> zeilen = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> e : w.entrySet()) {
            String pos = e.getKey();
            Art art = pos.startsWith("Bilanzdifferenz") ? Art.PRUEFUNG
                    : pos.startsWith("=") ? Art.SUMME : Art.WERT;
            BigDecimal ytd = e.getValue().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            zeilen.add(new BilanzZeile(pos, art, e.getValue(), ytd));
        }
        return new BilanzBericht(mandant, jahr, monate, zeilen);
    }

    private static BigDecimal grp(Map<String, BigDecimal> g, String gruppe) {
        return g.getOrDefault(gruppe, BigDecimal.ZERO);
    }

    private static void put(Map<String, Map<String, BigDecimal>> w, String pos, String monat, BigDecimal v) {
        w.computeIfAbsent(pos, p -> new LinkedHashMap<>()).put(monat, v);
    }
}

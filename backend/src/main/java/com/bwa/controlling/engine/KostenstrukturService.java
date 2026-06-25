package com.bwa.controlling.engine;

import com.bwa.controlling.engine.KostenstrukturBericht.KostenartZeile;
import com.bwa.controlling.engine.KostenstrukturBericht.KostenstelleZeile;
import com.bwa.controlling.stammdaten.Mitarbeiter;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kostenstruktur-Analyse: Kostenarten (Wareneinsatz/Personal/Sonstige) je Monat mit Quoten vom
 * Umsatz, plus Lohnsumme je Kostenstelle. Basiert auf der GuV und den Mitarbeiterstammdaten.
 */
@Service
public class KostenstrukturService {

    private static final List<String> SONSTIGE_POSITIONEN = List.of(
            "- Raumkosten", "- Kfz-Kosten", "- Marketing & Werbung", "- Reisekosten",
            "- IT & Bürokosten", "- Versicherungen & Beiträge", "- Sonstige Betriebskosten");

    private final GuvService guvService;
    private final StammdatenService stammdaten;

    public KostenstrukturService(GuvService guvService, StammdatenService stammdaten) {
        this.guvService = guvService;
        this.stammdaten = stammdaten;
    }

    public KostenstrukturBericht berechne(String mandant, int jahr) {
        GuvBericht guv = guvService.berechne(mandant, jahr);

        List<KostenartZeile> kostenarten = guv.monate().stream().map(m -> {
            BigDecimal umsatz = guv.monat("Umsatzerlöse", m);
            BigDecimal wareneinsatz = guv.monat("- Wareneinsatz / Materialeinsatz", m);
            BigDecimal personal = guv.monat("- Personalaufwand", m);
            BigDecimal sonstige = SONSTIGE_POSITIONEN.stream()
                    .map(p -> guv.monat(p, m)).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal gesamt = wareneinsatz.add(personal).add(sonstige);
            return new KostenartZeile(m, umsatz,
                    wareneinsatz, prozent(wareneinsatz, umsatz),
                    personal, prozent(personal, umsatz),
                    sonstige, prozent(sonstige, umsatz),
                    gesamt, prozent(gesamt, umsatz));
        }).toList();

        return new KostenstrukturBericht(mandant, jahr, kostenarten, kostenstellen(mandant));
    }

    private List<KostenstelleZeile> kostenstellen(String mandant) {
        Map<String, BigDecimal> proStelle = new LinkedHashMap<>();
        for (Mitarbeiter m : stammdaten.mitarbeiterVon(mandant)) {
            String ks = m.getKostenstelle() == null ? "(ohne)" : m.getKostenstelle();
            proStelle.merge(ks, m.getMonatslohn(), BigDecimal::add);
        }
        BigDecimal gesamt = proStelle.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return proStelle.entrySet().stream()
                .map(e -> new KostenstelleZeile(e.getKey(), e.getValue(), prozent(e.getValue(), gesamt)))
                .toList();
    }

    private static BigDecimal prozent(BigDecimal zaehler, BigDecimal nenner) {
        if (nenner == null || nenner.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return zaehler.multiply(BigDecimal.valueOf(100)).divide(nenner, 1, RoundingMode.HALF_UP);
    }
}

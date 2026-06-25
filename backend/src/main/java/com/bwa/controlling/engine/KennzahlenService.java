package com.bwa.controlling.engine;

import com.bwa.controlling.engine.Kennzahl.Ampel;
import com.bwa.controlling.engine.Kennzahl.Richtung;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Berechnet die BWA-Kennzahlen mit Ampelbewertung aus GuV, Bilanz und Mitarbeiterdaten.
 * Hinweis: im Excel-Original waren die Kennzahlenwerte hartkodierte Konstanten — hier werden
 * sie live aus den Buchungsdaten berechnet. Zielwerte stammen aus den Einstellungen
 * (EBIT-Marge, Liquidität) bzw. aus fachlichen Defaults.
 */
@Service
public class KennzahlenService {

    private static final List<String> NICHT_PERSONAL_KOSTEN = List.of(
            "- Raumkosten", "- Kfz-Kosten", "- Marketing & Werbung", "- Reisekosten",
            "- IT & Bürokosten", "- Versicherungen & Beiträge", "- Sonstige Betriebskosten");

    private final GuvService guvService;
    private final BilanzService bilanzService;
    private final StammdatenService stammdaten;

    public KennzahlenService(GuvService guvService, BilanzService bilanzService, StammdatenService stammdaten) {
        this.guvService = guvService;
        this.bilanzService = bilanzService;
        this.stammdaten = stammdaten;
    }

    public List<Kennzahl> berechne(String mandant, int jahr) {
        GuvBericht guv = guvService.berechne(mandant, jahr);
        BilanzBericht bilanz = bilanzService.berechne(mandant, jahr);

        BigDecimal umsatz = guv.ytd("Umsatzerlöse");
        BigDecimal gesamtleistung = guv.ytd("= Gesamtleistung");
        BigDecimal rohertrag = guv.ytd("= Rohertrag (DB I)");
        BigDecimal ebit = guv.ytd("= EBIT (Betriebsergebnis)");
        BigDecimal jahresueberschuss = guv.ytd("= Jahresüberschuss / -fehlbetrag");
        BigDecimal personal = guv.ytd("- Personalaufwand");
        BigDecimal wareneinsatz = guv.ytd("- Wareneinsatz / Materialeinsatz");

        BigDecimal sonstigeKosten = BigDecimal.ZERO;
        for (String pos : NICHT_PERSONAL_KOSTEN) {
            sonstigeKosten = sonstigeKosten.add(guv.ytd(pos));
        }

        BigDecimal liquideMittel = bilanz.ytd("3. Bankguthaben").add(bilanz.ytd("4. Kasse"));
        BigDecimal forderungen = bilanz.ytd("2. Forderungen aus LuL");
        BigDecimal kurzfristigeVerb = bilanz.ytd("= Verbindlichkeiten gesamt");

        long maAnzahl = stammdaten.mitarbeiterAnzahl(mandant);

        BigDecimal zielEbit = stammdaten.zielwert("Ziel-EBIT-Marge %").orElse(new BigDecimal("18"));
        BigDecimal zielLiq = stammdaten.zielwert("Ziel-Liquidität %").orElse(new BigDecimal("120"));

        List<Kennzahl> k = new ArrayList<>();
        k.add(quotenKennzahl("Rohertragsquote", rohertrag, gesamtleistung, new BigDecimal("65"),
                Richtung.HOEHER_BESSER, "Rohertrag / Gesamtleistung. Hoch = stabile Wertschöpfung."));
        k.add(quotenKennzahl("EBIT-Marge", ebit, umsatz, zielEbit,
                Richtung.HOEHER_BESSER, "Operatives Ergebnis / Umsatz."));
        k.add(quotenKennzahl("Personalquote", personal, umsatz, new BigDecimal("25"),
                Richtung.NIEDRIGER_BESSER, "Personalkosten / Umsatz. Niedrig = effizient."));
        k.add(quotenKennzahl("Materialquote", wareneinsatz, umsatz, new BigDecimal("35"),
                Richtung.NIEDRIGER_BESSER, "Wareneinsatz / Umsatz."));
        k.add(quotenKennzahl("Sonstigenquote", sonstigeKosten, umsatz, new BigDecimal("20"),
                Richtung.NIEDRIGER_BESSER, "Sonstige Betriebskosten / Umsatz."));
        k.add(quotenKennzahl("Rentabilität (RoS)", jahresueberschuss, umsatz, new BigDecimal("20"),
                Richtung.HOEHER_BESSER, "Jahresüberschuss / Umsatz."));
        k.add(quotenKennzahl("Liquidität 1. Grades", liquideMittel, kurzfristigeVerb, zielLiq,
                Richtung.HOEHER_BESSER, "Liquide Mittel / kurzfristige Verbindlichkeiten."));
        k.add(quotenKennzahl("Liquidität 2. Grades", liquideMittel.add(forderungen), kurzfristigeVerb,
                new BigDecimal("150"), Richtung.HOEHER_BESSER,
                "(Liquide Mittel + Forderungen) / kurzfristige Verbindlichkeiten."));
        k.add(new Kennzahl("Deckungsbeitrag DB I", rohertrag.setScale(2, RoundingMode.HALF_UP), "€",
                null, Ampel.NEUTRAL, Richtung.KEINE, "Rohertrag gesamt (absolut)."));
        k.add(new Kennzahl("Umsatz je Mitarbeiter", jeMitarbeiter(umsatz, maAnzahl), "€/MA",
                null, Ampel.NEUTRAL, Richtung.KEINE, "Umsatz / Mitarbeiteranzahl (informativ)."));
        k.add(new Kennzahl("EBIT je Mitarbeiter", jeMitarbeiter(ebit, maAnzahl), "€/MA",
                null, Ampel.NEUTRAL, Richtung.KEINE, "EBIT / Mitarbeiteranzahl (informativ)."));
        return k;
    }

    private static Kennzahl quotenKennzahl(String name, BigDecimal zaehler, BigDecimal nenner,
                                           BigDecimal ziel, Richtung richtung, String interpretation) {
        BigDecimal wert = prozent(zaehler, nenner);
        return new Kennzahl(name, wert, "%", ziel, ampel(wert, ziel, richtung), richtung, interpretation);
    }

    /** Anteil in Prozent, auf eine Nachkommastelle gerundet. */
    private static BigDecimal prozent(BigDecimal zaehler, BigDecimal nenner) {
        if (nenner == null || nenner.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return zaehler.multiply(BigDecimal.valueOf(100)).divide(nenner, 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal jeMitarbeiter(BigDecimal wert, long maAnzahl) {
        if (maAnzahl == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return wert.divide(BigDecimal.valueOf(maAnzahl), 2, RoundingMode.HALF_UP);
    }

    static Ampel ampel(BigDecimal wert, BigDecimal ziel, Richtung richtung) {
        if (ziel == null || richtung == Richtung.KEINE) {
            return Ampel.NEUTRAL;
        }
        return switch (richtung) {
            case HOEHER_BESSER -> {
                if (wert.compareTo(ziel) >= 0) yield Ampel.GRUEN;
                yield wert.compareTo(ziel.multiply(new BigDecimal("0.9"))) >= 0 ? Ampel.GELB : Ampel.ROT;
            }
            case NIEDRIGER_BESSER -> {
                if (wert.compareTo(ziel) <= 0) yield Ampel.GRUEN;
                yield wert.compareTo(ziel.multiply(new BigDecimal("1.1"))) <= 0 ? Ampel.GELB : Ampel.ROT;
            }
            case KEINE -> Ampel.NEUTRAL;
        };
    }
}

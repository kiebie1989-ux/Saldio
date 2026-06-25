package com.bwa.controlling.ki;

import com.bwa.controlling.engine.Kennzahl;
import com.bwa.controlling.engine.Kennzahl.Ampel;
import com.bwa.controlling.ki.Mandantenbericht.Abschnitt;
import com.bwa.controlling.ki.Mandantenbericht.BereichsAnalyse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Deterministischer Generator: leitet Bereichsanalysen und Managementkommentar regelbasiert aus
 * den Kennzahlen und der Ampelbewertung ab. Default und Fallback, wenn keine KI aktiv/erreichbar ist.
 */
@Component
public class RegelbasierterKommentarGenerator implements KommentarGenerator {

    /** Bereich -> zugrunde liegende Kennzahl. */
    private static final Map<String, String> BEREICHE = Map.of(
            "EBIT", "EBIT-Marge",
            "Rohertrag", "Rohertragsquote",
            "Liquidität", "Liquidität 1. Grades",
            "Rentabilität", "Rentabilität (RoS)",
            "Personal", "Personalquote",
            "Sonstiges", "Sonstigenquote");

    private static final List<String> BEREICH_REIHENFOLGE =
            List.of("EBIT", "Rohertrag", "Liquidität", "Rentabilität", "Personal", "Sonstiges");

    @Override
    public String quelle() {
        return "regelbasiert";
    }

    @Override
    public Mandantenbericht erzeuge(BerichtsFakten f) {
        return new Mandantenbericht(f.mandant(), f.jahr(), quelle(), bereiche(f), managementkommentar(f));
    }

    private List<BereichsAnalyse> bereiche(BerichtsFakten f) {
        List<BereichsAnalyse> result = new ArrayList<>();
        for (String bereich : BEREICH_REIHENFOLGE) {
            Kennzahl k = f.kennzahl(BEREICHE.get(bereich)).orElse(null);
            if (k == null) {
                continue;
            }
            String analyse = "%s: %s liegt bei %s %s%s. %s".formatted(
                    bereich, k.name(), k.wert(), k.einheit(),
                    k.zielwert() != null ? " (Ziel %s)".formatted(k.zielwert()) : "",
                    ampelSatz(k.ampel()));
            result.add(new BereichsAnalyse(bereich, k.ampel(), bewertung(k.ampel()), massnahme(k.ampel()), analyse));
        }
        return result;
    }

    private List<Abschnitt> managementkommentar(BerichtsFakten f) {
        var kpis = f.kpis();
        BigDecimal marge = kpis.ebitMarge();
        String wachstumsHinweis = marge.compareTo(BigDecimal.valueOf(15)) >= 0
                ? "Die Ertragskraft liegt deutlich über dem Mittelstandsschnitt."
                : "Die Ertragskraft ist solide, bietet aber Optimierungspotenzial.";

        List<Abschnitt> a = new ArrayList<>();
        a.add(new Abschnitt("1. Geschäftsentwicklung",
                "Der Umsatz im Jahr %d beläuft sich auf %s. %s"
                        .formatted(f.jahr(), euro(kpis.umsatzYtd()), wachstumsHinweis)));
        a.add(new Abschnitt("2. Ertragslage",
                "Rohertrag %s, EBIT %s bei einer EBIT-Marge von %s %%. %s"
                        .formatted(euro(kpis.rohertragYtd()), euro(kpis.ebitYtd()), marge,
                                bewertungSatz("EBIT-Marge", f))));
        a.add(new Abschnitt("3. Kostenbewertung", kostenSatz(f)));
        a.add(new Abschnitt("4. Liquidität & Finanzlage", liquiditaetSatz(f)));
        a.add(new Abschnitt("5. Personalentwicklung",
                "%d Mitarbeiter im Mandanten. %s"
                        .formatted(kpis.mitarbeiter(), bewertungSatz("Personalquote", f))));
        a.add(new Abschnitt("6. Ausblick & Empfehlung", ausblickSatz(f)));
        return a;
    }

    private static String kostenSatz(BerichtsFakten f) {
        StringBuilder sb = new StringBuilder();
        for (String name : List.of("Materialquote", "Personalquote", "Sonstigenquote")) {
            f.kennzahl(name).ifPresent(k -> sb.append("%s %s %% (%s); "
                    .formatted(k.name(), k.wert(), ampelWort(k.ampel()))));
        }
        boolean kritisch = f.kennzahl("Sonstigenquote").map(k -> k.ampel() == Ampel.ROT).orElse(false);
        sb.append(kritisch
                ? "KRITISCH: Die Sonstigenquote überschreitet den Zielkorridor — Kostenblock aufschlüsseln."
                : "Die Kostenstruktur liegt im Rahmen.");
        return sb.toString();
    }

    private static String liquiditaetSatz(BerichtsFakten f) {
        String l1 = f.kennzahl("Liquidität 1. Grades").map(k -> k.wert() + " %").orElse("k.A.");
        String l2 = f.kennzahl("Liquidität 2. Grades").map(k -> k.wert() + " %").orElse("k.A.");
        boolean solvent = f.kennzahl("Liquidität 1. Grades").map(k -> k.ampel() == Ampel.GRUEN).orElse(false);
        return "Liquidität 1. Grades %s, 2. Grades %s. %s".formatted(l1, l2,
                solvent ? "Die kurzfristige Zahlungsfähigkeit ist komfortabel gesichert."
                        : "Die Liquiditätslage sollte eng überwacht werden.");
    }

    private static String ausblickSatz(BerichtsFakten f) {
        long rot = f.kennzahlen().stream().filter(k -> k.ampel() == Ampel.ROT).count();
        if (rot == 0) {
            return "Alle bewerteten Kennzahlen liegen im grünen oder gelben Bereich. "
                    + "Empfehlung: Niveau halten und Liquiditätsüberschuss strukturieren.";
        }
        return ("%d Kennzahl(en) im roten Bereich. Empfehlung: priorisierte Ursachenanalyse, "
                + "insbesondere bei den Kostenquoten, vor der nächsten Planungsrunde.").formatted(rot);
    }

    private static String bewertungSatz(String kennzahl, BerichtsFakten f) {
        return f.kennzahl(kennzahl).map(k -> "Bewertung: " + bewertung(k.ampel()) + ".").orElse("");
    }

    private static String ampelSatz(Ampel a) {
        return switch (a) {
            case GRUEN -> "Der Wert liegt im Zielkorridor.";
            case GELB -> "Der Wert liegt nahe der Zielschwelle und ist zu beobachten.";
            case ROT -> "Der Wert verfehlt das Ziel deutlich — Handlungsbedarf.";
            case NEUTRAL -> "Informativer Wert ohne Zielbewertung.";
        };
    }

    private static String bewertung(Ampel a) {
        return switch (a) {
            case GRUEN -> "im grünen Bereich";
            case GELB -> "unter Beobachtung";
            case ROT -> "kritisch";
            case NEUTRAL -> "informativ";
        };
    }

    private static String massnahme(Ampel a) {
        return switch (a) {
            case GRUEN -> "Niveau halten";
            case GELB -> "eng überwachen";
            case ROT -> "Ursachen analysieren";
            case NEUTRAL -> "—";
        };
    }

    private static String ampelWort(Ampel a) {
        return switch (a) {
            case GRUEN -> "grün";
            case GELB -> "gelb";
            case ROT -> "rot";
            case NEUTRAL -> "neutral";
        };
    }

    private static String euro(BigDecimal v) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        nf.setMaximumFractionDigits(0);
        return nf.format(v == null ? BigDecimal.ZERO : v);
    }
}

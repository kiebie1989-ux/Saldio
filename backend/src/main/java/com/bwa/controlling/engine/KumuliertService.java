package com.bwa.controlling.engine;

import com.bwa.controlling.engine.KumuliertBericht.MandantKennzahl;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * Aggregiert die GuV-Ergebnisse über mehrere Mandanten je nach Berichtsmodus
 * (Einzeln = aktiver Mandant, Kumuliert = Flag in_kumulierung, Final = Flag im_finalbericht).
 */
@Service
public class KumuliertService {

    public enum Modus { EINZELN, KUMULIERT, FINAL }

    private static final String UMSATZ = "Umsatzerlöse";
    private static final String ROHERTRAG = "= Rohertrag (DB I)";
    private static final String EBIT = "= EBIT (Betriebsergebnis)";

    private final GuvService guvService;
    private final StammdatenService stammdaten;

    public KumuliertService(GuvService guvService, StammdatenService stammdaten) {
        this.guvService = guvService;
        this.stammdaten = stammdaten;
    }

    public KumuliertBericht berechne(Modus modus, int jahr) {
        return berechne(modus, jahr, null);
    }

    /**
     * @param erlaubte Mandanten, die einbezogen werden dürfen (null = keine Einschränkung).
     */
    public KumuliertBericht berechne(Modus modus, int jahr, Set<String> erlaubte) {
        List<String> namen = switch (modus) {
            case EINZELN -> stammdaten.aktiverMandant().map(List::of).orElseGet(List::of);
            case KUMULIERT -> stammdaten.kumulierteMandanten();
            case FINAL -> stammdaten.finaleMandanten();
        };
        if (erlaubte != null) {
            namen = namen.stream().filter(erlaubte::contains).toList();
        }

        List<MandantKennzahl> zeilen = namen.stream().map(name -> {
            GuvBericht guv = guvService.berechne(name, jahr);
            return kennzahl(name, guv.ytd(UMSATZ), guv.ytd(ROHERTRAG), guv.ytd(EBIT));
        }).toList();

        BigDecimal umsatz = summe(zeilen, MandantKennzahl::umsatz);
        BigDecimal rohertrag = summe(zeilen, MandantKennzahl::rohertrag);
        BigDecimal ebit = summe(zeilen, MandantKennzahl::ebit);
        MandantKennzahl summe = kennzahl("GESAMT (" + modus + ")", umsatz, rohertrag, ebit);

        return new KumuliertBericht(modus.name(), jahr, zeilen, summe);
    }

    private static MandantKennzahl kennzahl(String name, BigDecimal umsatz, BigDecimal rohertrag, BigDecimal ebit) {
        return new MandantKennzahl(name, umsatz, rohertrag,
                prozent(rohertrag, umsatz), ebit, prozent(ebit, umsatz));
    }

    private static BigDecimal summe(List<MandantKennzahl> zeilen,
                                    java.util.function.Function<MandantKennzahl, BigDecimal> feld) {
        return zeilen.stream().map(feld).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal prozent(BigDecimal zaehler, BigDecimal nenner) {
        if (nenner == null || nenner.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return zaehler.multiply(BigDecimal.valueOf(100)).divide(nenner, 1, RoundingMode.HALF_UP);
    }
}

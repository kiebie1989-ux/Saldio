package com.bwa.controlling.engine;

import com.bwa.controlling.engine.DashboardBericht.Kpis;
import com.bwa.controlling.engine.DashboardBericht.MonatsWert;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Stellt die Dashboard-Kennzahlen und die Monatsreihe aus der GuV zusammen. */
@Service
public class DashboardService {

    private static final String UMSATZ = "Umsatzerlöse";
    private static final String ROHERTRAG = "= Rohertrag (DB I)";
    private static final String EBIT = "= EBIT (Betriebsergebnis)";

    private final GuvService guvService;
    private final StammdatenService stammdaten;

    public DashboardService(GuvService guvService, StammdatenService stammdaten) {
        this.guvService = guvService;
        this.stammdaten = stammdaten;
    }

    public DashboardBericht berechne(String mandant, int jahr) {
        GuvBericht guv = guvService.berechne(mandant, jahr);

        BigDecimal umsatzYtd = guv.ytd(UMSATZ);
        BigDecimal rohertragYtd = guv.ytd(ROHERTRAG);
        BigDecimal ebitYtd = guv.ytd(EBIT);
        BigDecimal ebitMarge = umsatzYtd.signum() == 0 ? BigDecimal.ZERO.setScale(1)
                : ebitYtd.multiply(BigDecimal.valueOf(100)).divide(umsatzYtd, 1, RoundingMode.HALF_UP);

        Kpis kpis = new Kpis(umsatzYtd, rohertragYtd, ebitYtd, ebitMarge,
                stammdaten.mitarbeiterAnzahl(mandant));

        List<MonatsWert> reihe = guv.monate().stream()
                .map(m -> new MonatsWert(m, wert(guv, UMSATZ, m), wert(guv, ROHERTRAG, m), wert(guv, EBIT, m)))
                .toList();

        return new DashboardBericht(mandant, jahr, kpis, reihe);
    }

    private static BigDecimal wert(GuvBericht guv, String position, String monat) {
        return guv.zeilen().stream()
                .filter(z -> z.position().equals(position)).findFirst()
                .map(z -> z.monate().getOrDefault(monat, BigDecimal.ZERO))
                .orElse(BigDecimal.ZERO);
    }
}

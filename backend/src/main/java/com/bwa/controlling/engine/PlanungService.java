package com.bwa.controlling.engine;

import com.bwa.controlling.engine.PlanungBericht.Jahreswerte;
import com.bwa.controlling.engine.PlanungBericht.PlanZeile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Forecast auf Basis der IST-Monate. Monate bis einschließlich {@code bisMonat} stammen aus den
 * tatsächlichen Buchungen; die Folgemonate werden als Durchschnitt der IST-Monate je Szenario
 * hochgerechnet. Transparent und deterministisch (im Excel waren die Planwerte manuell gesetzt).
 */
@Service
public class PlanungService {

    public enum Szenario {
        PESSIMISTISCH("0.95"), BASIS("1.00"), OPTIMISTISCH("1.05");
        final BigDecimal faktor;
        Szenario(String f) { this.faktor = new BigDecimal(f); }
    }

    private static final String UMSATZ = "Umsatzerlöse";
    private static final String ROHERTRAG = "= Rohertrag (DB I)";
    private static final String EBIT = "= EBIT (Betriebsergebnis)";

    private final GuvService guvService;

    public PlanungService(GuvService guvService) {
        this.guvService = guvService;
    }

    public PlanungBericht berechne(String mandant, int jahr, int bisMonat, Szenario szenario) {
        int grenze = Math.min(Math.max(bisMonat, 1), 12);
        GuvBericht guv = guvService.berechne(mandant, jahr);

        BigDecimal istUmsatz = BigDecimal.ZERO, istRohertrag = BigDecimal.ZERO, istEbit = BigDecimal.ZERO;
        List<PlanZeile> zeilen = new ArrayList<>(12);

        // IST-Monate
        for (int m = 1; m <= grenze; m++) {
            String monat = "%d-%02d".formatted(jahr, m);
            BigDecimal u = guv.monat(UMSATZ, monat);
            BigDecimal r = guv.monat(ROHERTRAG, monat);
            BigDecimal e = guv.monat(EBIT, monat);
            istUmsatz = istUmsatz.add(u);
            istRohertrag = istRohertrag.add(r);
            istEbit = istEbit.add(e);
            zeilen.add(new PlanZeile(monat, "IST", u, r, e));
        }

        // Forecast = Durchschnitt der IST-Monate * Szenariofaktor
        BigDecimal planUmsatz = forecast(istUmsatz, grenze, szenario);
        BigDecimal planRohertrag = forecast(istRohertrag, grenze, szenario);
        BigDecimal planEbit = forecast(istEbit, grenze, szenario);

        BigDecimal sumPlanU = BigDecimal.ZERO, sumPlanR = BigDecimal.ZERO, sumPlanE = BigDecimal.ZERO;
        for (int m = grenze + 1; m <= 12; m++) {
            zeilen.add(new PlanZeile("%d-%02d".formatted(jahr, m), "PLAN", planUmsatz, planRohertrag, planEbit));
            sumPlanU = sumPlanU.add(planUmsatz);
            sumPlanR = sumPlanR.add(planRohertrag);
            sumPlanE = sumPlanE.add(planEbit);
        }

        Jahreswerte prognose = new Jahreswerte(
                istUmsatz.add(sumPlanU), istRohertrag.add(sumPlanR), istEbit.add(sumPlanE));

        return new PlanungBericht(mandant, jahr, grenze, szenario.name(), zeilen, prognose);
    }

    private static BigDecimal forecast(BigDecimal istSumme, int monate, Szenario szenario) {
        if (monate == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return istSumme.divide(BigDecimal.valueOf(monate), 10, RoundingMode.HALF_UP)
                .multiply(szenario.faktor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

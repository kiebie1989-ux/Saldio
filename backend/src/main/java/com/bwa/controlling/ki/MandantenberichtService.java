package com.bwa.controlling.ki;

import com.bwa.controlling.engine.DashboardService;
import com.bwa.controlling.engine.KennzahlenService;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Stellt den Mandantenbericht zusammen: berechnet die Fakten (Kennzahlen + Dashboard) und wählt
 * den Textgenerator. KI wird nur genutzt, wenn die Einstellung "KI-Kommentar" aktiv ist, ein
 * Provider konfiguriert und verfügbar ist — sonst regelbasiert.
 */
@Service
public class MandantenberichtService {

    private final KennzahlenService kennzahlenService;
    private final DashboardService dashboardService;
    private final StammdatenService stammdaten;
    private final RegelbasierterKommentarGenerator regelbasiert;
    private final LlmKommentarGenerator llm;
    private final String provider;

    public MandantenberichtService(KennzahlenService kennzahlenService,
                                   DashboardService dashboardService,
                                   StammdatenService stammdaten,
                                   RegelbasierterKommentarGenerator regelbasiert,
                                   LlmKommentarGenerator llm,
                                   @Value("${bwa.ki.provider:none}") String provider) {
        this.kennzahlenService = kennzahlenService;
        this.dashboardService = dashboardService;
        this.stammdaten = stammdaten;
        this.regelbasiert = regelbasiert;
        this.llm = llm;
        this.provider = provider;
    }

    public Mandantenbericht erzeuge(String mandant, int jahr) {
        BerichtsFakten fakten = new BerichtsFakten(mandant, jahr,
                dashboardService.berechne(mandant, jahr).kpis(),
                kennzahlenService.berechne(mandant, jahr));
        return waehleGenerator().erzeuge(fakten);
    }

    private KommentarGenerator waehleGenerator() {
        boolean kiAktiv = stammdaten.einstellung("KI-Kommentar")
                .map(w -> w.equalsIgnoreCase("Aktiv")).orElse(false);
        boolean providerGesetzt = provider != null && !provider.equalsIgnoreCase("none");
        if (kiAktiv && providerGesetzt && llm.verfuegbar()) {
            return llm;
        }
        return regelbasiert;
    }
}

package com.bwa.controlling.engine;

import com.bwa.controlling.benutzer.MandantenZugriffService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST-Zugriff auf die berechneten Auswertungen: GuV, Bilanz, Kennzahlen, Dashboard. */
@RestController
@RequestMapping("/api")
public class EngineController {

    private final GuvService guvService;
    private final BilanzService bilanzService;
    private final KennzahlenService kennzahlenService;
    private final DashboardService dashboardService;
    private final KostenstrukturService kostenstrukturService;
    private final KumuliertService kumuliertService;
    private final PlanungService planungService;
    private final MandantenZugriffService zugriff;

    public EngineController(GuvService guvService, BilanzService bilanzService,
                            KennzahlenService kennzahlenService, DashboardService dashboardService,
                            KostenstrukturService kostenstrukturService, KumuliertService kumuliertService,
                            PlanungService planungService, MandantenZugriffService zugriff) {
        this.guvService = guvService;
        this.bilanzService = bilanzService;
        this.kennzahlenService = kennzahlenService;
        this.dashboardService = dashboardService;
        this.kostenstrukturService = kostenstrukturService;
        this.kumuliertService = kumuliertService;
        this.planungService = planungService;
        this.zugriff = zugriff;
    }

    @GetMapping("/guv")
    public GuvBericht guv(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return guvService.berechne(mandant, jahr);
    }

    @GetMapping("/bilanz")
    public BilanzBericht bilanz(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return bilanzService.berechne(mandant, jahr);
    }

    @GetMapping("/kennzahlen")
    public List<Kennzahl> kennzahlen(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return kennzahlenService.berechne(mandant, jahr);
    }

    @GetMapping("/dashboard")
    public DashboardBericht dashboard(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return dashboardService.berechne(mandant, jahr);
    }

    @GetMapping("/kostenstruktur")
    public KostenstrukturBericht kostenstruktur(@RequestParam String mandant, @RequestParam int jahr) {
        zugriff.pruefe(mandant);
        return kostenstrukturService.berechne(mandant, jahr);
    }

    @GetMapping("/kumuliert")
    public KumuliertBericht kumuliert(@RequestParam(defaultValue = "KUMULIERT") KumuliertService.Modus modus,
                                      @RequestParam int jahr) {
        // Nur erlaubte Mandanten in die Kumulierung einbeziehen.
        return kumuliertService.berechne(modus, jahr, zugriff.erlaubteMandanten());
    }

    @GetMapping("/planung")
    public PlanungBericht planung(@RequestParam String mandant, @RequestParam int jahr,
                                  @RequestParam(defaultValue = "9") int bisMonat,
                                  @RequestParam(defaultValue = "BASIS") PlanungService.Szenario szenario) {
        zugriff.pruefe(mandant);
        return planungService.berechne(mandant, jahr, bisMonat, szenario);
    }
}

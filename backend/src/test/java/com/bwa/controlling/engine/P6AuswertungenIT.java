package com.bwa.controlling.engine;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.engine.KostenstrukturBericht.KostenartZeile;
import com.bwa.controlling.engine.KostenstrukturBericht.KostenstelleZeile;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifiziert die drei P6-Auswertungen (Kostenstruktur, Kumuliert/Final, Planung) gegen Golden-Werte. */
@SpringBootTest
@Transactional
class P6AuswertungenIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired KostenstrukturService kostenstrukturService;
    @Autowired KumuliertService kumuliertService;
    @Autowired PlanungService planungService;

    @BeforeEach
    void importiere() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    @Test
    void kostenstrukturJanuarUndKostenstellen() {
        KostenstrukturBericht b = kostenstrukturService.berechne(MANDANT, 2025);

        KostenartZeile jan = b.kostenarten().stream()
                .filter(k -> k.monat().equals("2025-01")).findFirst().orElseThrow();
        assertThat(jan.umsatz()).isEqualByComparingTo("92300");
        assertThat(jan.wareneinsatz()).isEqualByComparingTo("31000");
        assertThat(jan.personal()).isEqualByComparingTo("22000");
        // WE-Quote Jan = 31000/92300*100 = 33,6
        assertThat(jan.weQuote()).isEqualByComparingTo("33.6");

        // Kostenstellen-Lohnsumme (Mustermann): IT 9500, Sales 6100, Finance 4800
        KostenstelleZeile it = b.kostenstellen().stream()
                .filter(k -> k.kostenstelle().equals("IT")).findFirst().orElseThrow();
        assertThat(it.personalkosten()).isEqualByComparingTo("9500");
    }

    @Test
    void kumuliertSummiertUeberMandanten() {
        // Kumuliert (in_kumulierung): Mustermann (1.239.460) + Beispiel Handel (102.000) + Alpha/Beta (0)
        KumuliertBericht b = kumuliertService.berechne(KumuliertService.Modus.KUMULIERT, 2025);
        assertThat(b.summe().umsatz()).isEqualByComparingTo("1341460");
        assertThat(b.mandanten()).extracting(KumuliertBericht.MandantKennzahl::mandant)
                .contains("Mustermann GmbH", "Beispiel Handel GmbH");
    }

    @Test
    void einzelmodusNutztAktivenMandanten() {
        KumuliertBericht b = kumuliertService.berechne(KumuliertService.Modus.EINZELN, 2025);
        assertThat(b.mandanten()).hasSize(1);
        assertThat(b.mandanten().get(0).mandant()).isEqualTo("Mustermann GmbH");
        assertThat(b.mandanten().get(0).umsatz()).isEqualByComparingTo("1239460");
    }

    @Test
    void planungForecastBasisAbSeptember() {
        // IST Jan-Sep Umsatz = 898.460; Forecast Basis = avg(=99828,89) je Okt/Nov/Dez
        PlanungBericht b = planungService.berechne(MANDANT, 2025, 9, PlanungService.Szenario.BASIS);

        long ist = b.zeilen().stream().filter(z -> z.typ().equals("IST")).count();
        long plan = b.zeilen().stream().filter(z -> z.typ().equals("PLAN")).count();
        assertThat(ist).isEqualTo(9);
        assertThat(plan).isEqualTo(3);

        BigDecimal istUmsatz = b.zeilen().stream().filter(z -> z.typ().equals("IST"))
                .map(PlanungBericht.PlanZeile::umsatz).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(istUmsatz).isEqualByComparingTo("898460");

        // Jahresprognose Basis = 898460 + 3 * (898460/9) = 1.197.946,67
        assertThat(b.jahresprognose().umsatz()).isEqualByComparingTo("1197946.67");
    }

    @Test
    void optimistischesSzenarioLiegtUeberBasis() {
        BigDecimal basis = planungService.berechne(MANDANT, 2025, 9, PlanungService.Szenario.BASIS)
                .jahresprognose().umsatz();
        BigDecimal optimistisch = planungService.berechne(MANDANT, 2025, 9, PlanungService.Szenario.OPTIMISTISCH)
                .jahresprognose().umsatz();
        assertThat(optimistisch).isGreaterThan(basis);
    }
}

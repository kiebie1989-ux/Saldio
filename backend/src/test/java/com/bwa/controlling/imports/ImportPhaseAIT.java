package com.bwa.controlling.imports;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.engine.GuvBericht;
import com.bwa.controlling.engine.GuvService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase-A-Verhalten: EXTF-Mandantenauflösung, Doppel-Import-Schutz, Vorzeichen im echten Import. */
@SpringBootTest
@Transactional
class ImportPhaseAIT extends AbstractPostgresIT {

    @Autowired ImportService importService;
    @Autowired BuchungRepository buchungRepository;
    @Autowired GuvService guvService;

    private InputStream sample(String name) {
        return getClass().getResourceAsStream("/samples/" + name);
    }

    @Test
    void extfMandantennummerWirdAufgeloestUndErscheintImGuv() {
        ImportBatch batch = importService.importieren(ImportService.Quelle.EXTF,
                "EXTF_Buchungsstapel_2025-01.csv", sample("EXTF_Buchungsstapel_2025-01.csv"));

        // 73841 -> "Mustermann GmbH"
        List<Buchung> rows = buchungRepository.findByImportBatchId(batch.getId());
        assertThat(rows).isNotEmpty();
        assertThat(rows).allSatisfy(b -> assertThat(b.getMandant()).isEqualTo("Mustermann GmbH"));
        assertThat(batch.getZeilenWarnung()).isZero();

        // ...und taucht damit in der GuV des benannten Mandanten auf (Umsatz Jan = 92.300)
        GuvBericht guv = guvService.berechne("Mustermann GmbH", 2025);
        assertThat(guv.monat("Umsatzerlöse", "2025-01")).isEqualByComparingTo("92300");
    }

    @Test
    void unbekannteMandantennummerWirdMarkiert() {
        String extf = ("\"EXTF\";700;21;\"Buchungsstapel\";7;20250201;;;;;1001;99999;20250101;4;;;\n"
                + "Umsatz (ohne Soll/Haben-Kz);Soll/Haben-Kennzeichen;Konto;Belegdatum;Buchungstext\n"
                + "10,00;H;8000;1501;\"Test\"\n").replace("\n", "\r\n");
        ImportBatch batch = importService.importieren(ImportService.Quelle.EXTF, "x.csv",
                new ByteArrayInputStream(extf.getBytes(StandardCharsets.UTF_8)));

        assertThat(batch.getZeilenWarnung()).isEqualTo(1);
        assertThat(buchungRepository.findByImportBatchId(batch.getId()).get(0).getStatus())
                .isEqualTo("WARN_MANDANT_UNBEKANNT");
    }

    @Test
    void identischeDateiWirdAlsDoppelImportAbgelehnt() {
        importService.importieren(ImportService.Quelle.CSV, "bwa_simple.csv", sample("bwa_simple.csv"));

        assertThatThrownBy(() -> importService.importieren(
                ImportService.Quelle.CSV, "bwa_simple.csv", sample("bwa_simple.csv")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits importiert");
    }

    @Test
    void stornoReduziertDieGruppensumme() {
        // Zwei EXTF-Umsatzbuchungen: eine normal (H), eine Storno (S) -> Netto 0
        String extf = ("\"EXTF\";700;21;\"Buchungsstapel\";7;20250201;;;;;1001;73841;20250101;4;;;\n"
                + "Umsatz (ohne Soll/Haben-Kz);Soll/Haben-Kennzeichen;Konto;Belegdatum;Buchungstext\n"
                + "500,00;H;8000;1501;\"Umsatz\"\n"
                + "500,00;S;8000;1501;\"Storno\"\n").replace("\n", "\r\n");
        ImportBatch batch = importService.importieren(ImportService.Quelle.EXTF, "storno.csv",
                new ByteArrayInputStream(extf.getBytes(StandardCharsets.UTF_8)));

        var summe = buchungRepository.findByImportBatchId(batch.getId()).stream()
                .map(Buchung::getBetrag).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        assertThat(summe).isEqualByComparingTo("0.00");
    }
}

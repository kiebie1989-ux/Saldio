package com.bwa.controlling.imports;

import com.bwa.controlling.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Beweist den produktiven Importpfad: Datei -> Parser -> Mapping -> Postgres, gegen Golden Values. */
@SpringBootTest
@Transactional
class ImportServiceIT extends AbstractPostgresIT {

    @Autowired ImportService importService;
    @Autowired BuchungRepository buchungRepository;

    private InputStream sample(String name) {
        return getClass().getResourceAsStream("/samples/" + name);
    }

    @Test
    void csvImportReichertBwaGruppeUndPositionAn() {
        ImportBatch batch = importService.importieren(
                ImportService.Quelle.CSV, "bwa_simple.csv", sample("bwa_simple.csv"));

        assertThat(batch.getZeilenGesamt()).isEqualTo(6);
        assertThat(batch.getZeilenOk()).isEqualTo(6);
        assertThat(batch.getStatus()).isEqualTo("OK");

        List<Buchung> rows = buchungRepository.findByImportBatchId(batch.getId());
        Buchung umsatz = rows.stream()
                .filter(b -> b.getKonto().equals("8000") && b.getMonat().equals("2025-01"))
                .findFirst().orElseThrow();
        // Mapping (VLOOKUP) hat BWA-Gruppe UND GuV-Position ergänzt (standen nicht in der Datei)
        assertThat(umsatz.getBwaGruppe()).isEqualTo("Umsatz");
        assertThat(umsatz.getGuvBilanzPosition()).isEqualTo("GuV: Umsatzerlöse");
        assertThat(umsatz.getBetrag()).isEqualByComparingTo("92300.00");

        Buchung wareneinsatz = rows.stream()
                .filter(b -> b.getKonto().equals("3000")).findFirst().orElseThrow();
        assertThat(wareneinsatz.getBwaGruppe()).isEqualTo("Wareneinsatz");
        assertThat(wareneinsatz.getGuvBilanzPosition()).isEqualTo("GuV: Materialaufwand");
    }

    @Test
    void extfImportRekonstruiertMonatUndMappt() {
        ImportBatch batch = importService.importieren(
                ImportService.Quelle.EXTF, "EXTF_Buchungsstapel_2025-01.csv",
                sample("EXTF_Buchungsstapel_2025-01.csv"));

        assertThat(batch.getZeilenGesamt()).isEqualTo(4);
        List<Buchung> rows = buchungRepository.findByImportBatchId(batch.getId());
        assertThat(rows).allSatisfy(b -> assertThat(b.getMonat()).isEqualTo("2025-01"));

        BigDecimal summe = rows.stream()
                .map(Buchung::getBetrag).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(summe).isEqualByComparingTo("149500.00");

        Buchung wareneinsatz = rows.stream()
                .filter(b -> b.getKonto().equals("3000")).findFirst().orElseThrow();
        assertThat(wareneinsatz.getSollHaben()).isEqualTo("S");
        assertThat(wareneinsatz.getGuvBilanzPosition()).isEqualTo("GuV: Materialaufwand");
    }
}

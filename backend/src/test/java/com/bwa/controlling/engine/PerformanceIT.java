package com.bwa.controlling.engine;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance/Skalierung: großes synthetisches Volumen importieren und die Aggregation
 * gegen ein (großzügiges) Zeitbudget prüfen. Sichert die Indexnutzung und das Memo der
 * Festschreibungsprüfung ab.
 */
@SpringBootTest
@Transactional
class PerformanceIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";
    private static final String[] KONTEN = {"8000", "3000", "4100", "4830", "4210", "4600"};

    @Autowired ImportService importService;
    @Autowired GuvService guvService;

    @Test
    void grossesVolumenWirdImportiertUndSchnellAggregiert() {
        int proMonatJeKonto = 100; // 12 * 6 * 100 = 7200 Buchungen
        StringBuilder csv = new StringBuilder("Monat;Mandant;Konto;Kontobezeichnung;Betrag;Kostenstelle\n");
        for (int monat = 1; monat <= 12; monat++) {
            String mm = "2025-%02d".formatted(monat);
            for (int i = 0; i < proMonatJeKonto; i++) {
                for (String konto : KONTEN) {
                    csv.append(mm).append(";Mustermann GmbH;").append(konto)
                       .append(";Pos;100,00;KSt\n");
                }
            }
        }

        long t0 = System.currentTimeMillis();
        var batch = importService.importieren(ImportService.Quelle.CSV, "gross.csv",
                new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8)));
        long importMs = System.currentTimeMillis() - t0;
        assertThat(batch.getZeilenGesamt()).isEqualTo(12 * 6 * proMonatJeKonto);

        long t1 = System.currentTimeMillis();
        GuvBericht guv = guvService.berechne(MANDANT, 2025);
        long aggMs = System.currentTimeMillis() - t1;

        // Umsatz (Konto 8000): 12 Monate * 100 * 100,00 = 120.000
        assertThat(guv.ytd("Umsatzerlöse")).isEqualByComparingTo("120000");
        // Aggregation muss auch bei tausenden Zeilen flott bleiben (großzügiges Budget gegen Regressionen).
        assertThat(aggMs).isLessThan(5000L);
        System.out.printf("Performance: Import %d Zeilen in %d ms, Aggregation %d ms%n",
                batch.getZeilenGesamt(), importMs, aggMs);
    }
}

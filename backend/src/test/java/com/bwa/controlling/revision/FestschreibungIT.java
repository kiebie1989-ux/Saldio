package com.bwa.controlling.revision;

import com.bwa.controlling.AbstractPostgresIT;
import com.bwa.controlling.imports.ImportBatch;
import com.bwa.controlling.imports.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase B2: Festschreibung (Sperre + Hash-Kette), Append-only-Unveränderbarkeit, Storno. */
@SpringBootTest
@Transactional
class FestschreibungIT extends AbstractPostgresIT {

    private static final String MANDANT = "Mustermann GmbH";

    @Autowired ImportService importService;
    @Autowired FestschreibungService festschreibung;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void importiere() {
        importService.importieren(ImportService.Quelle.CSV, "saldenliste_2025_voll.csv",
                getClass().getResourceAsStream("/samples/saldenliste_2025_voll.csv"));
    }

    private InputStream csv(String inhalt) {
        return new ByteArrayInputStream(inhalt.replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void festschreibungSperrtWeitereBuchungen() {
        festschreibung.festschreibe(MANDANT, "2025-01");

        String neu = "Monat;Mandant;Konto;Kontobezeichnung;Betrag;Kostenstelle\n"
                + "2025-01;Mustermann GmbH;8000;Nachbuchung;10,00;X\n";
        assertThatThrownBy(() -> importService.importieren(ImportService.Quelle.CSV, "nach.csv", csv(neu)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("festgeschrieben");
    }

    @Test
    void hashKetteErkenntNachtraeglicheManipulation() {
        festschreibung.festschreibe(MANDANT, "2025-01");
        assertThat(festschreibung.pruefe(MANDANT).unveraendert()).isTrue();

        // Manipulation simulieren: zusätzliche Buchung direkt in die gesiegelte Periode einschleusen.
        jdbc.update("INSERT INTO buchung (quelle, monat, mandant, konto, betrag, status) "
                + "VALUES ('MANIPULATION','2025-01',?, '8000', 999.00, 'OK')", MANDANT);

        FestschreibungService.PruefErgebnis ergebnis = festschreibung.pruefe(MANDANT);
        assertThat(ergebnis.unveraendert()).isFalse();
        assertThat(ergebnis.abweichendeMonate()).contains("2025-01");
    }

    @Test
    void buchungIstAppendOnly() {
        Long id = jdbc.queryForObject(
                "SELECT id FROM buchung WHERE mandant=? AND monat='2025-01' LIMIT 1", Long.class, MANDANT);
        assertThatThrownBy(() -> jdbc.update("UPDATE buchung SET betrag = 0 WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void stornoErzeugtGegenstapelUndLaesstOriginalUnveraendert() {
        ImportBatch original = importService.importieren(ImportService.Quelle.CSV, "extra.csv",
                csv("Monat;Mandant;Konto;Kontobezeichnung;Betrag;Kostenstelle\n"
                        + "2025-01;Mustermann GmbH;8000;Sonderumsatz;500,00;X\n"));

        ImportBatch storno = importService.storniere(original.getId());
        assertThat(storno.getStorniertBatchId()).isEqualTo(original.getId());

        // Original (+500) + Storno (-500) = 0 netto in 8000 für diesen Vorgang
        var summe = jdbc.queryForObject(
                "SELECT COALESCE(SUM(betrag),0) FROM buchung WHERE import_batch_id IN (?,?)",
                java.math.BigDecimal.class, original.getId(), storno.getId());
        assertThat(summe).isEqualByComparingTo("0.00");

        // Zweiter Storno wird abgelehnt
        assertThatThrownBy(() -> importService.storniere(original.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits storniert");
    }
}

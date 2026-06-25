package com.bwa.poc.datev;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PoC-Gate: beweist den DATEV-Importpfad end-to-end gegen ein echtes PostgreSQL (Testcontainers).
 *
 * Geprüft:
 *  - Apache Commons CSV + Windows-1252 + deutsches Dezimalkomma (Umlaute, Tausenderpunkt)
 *  - DATEV-EXTF-Eigenbau: header-name-basiertes Parsing + WJ/TTMM -> Jahr-Rekonstruktion + S/H
 *  - Persistenz nach Postgres mit numeric-Geldtyp
 *  - Aggregation reproduziert die Excel-Golden-Values (Blatt 02_BWA_Import, Jan 2025)
 */
@Testcontainers
class DatevImportPocTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private InputStream sample(String name) {
        InputStream in = getClass().getResourceAsStream("/samples/" + name);
        assertThat(in).as("Beispieldatei %s", name).isNotNull();
        return in;
    }

    @Test
    void importiertCsvUndExtfUndReproduziertGoldenValues() throws Exception {
        // --- Parse: vereinfachtes BWA-CSV ---
        List<BuchungRecord> csv = new SimpleBwaCsvParser().parse(sample("bwa_simple.csv"));
        assertThat(csv).hasSize(6);
        // Umlaut aus CP1252 korrekt dekodiert
        assertThat(csv.get(0).bezeichnung()).isEqualTo("Umsatzerlöse 19%");
        // deutsches Dezimalkomma + Tausenderpunkt korrekt geparst
        assertThat(csv.get(0).betrag()).isEqualByComparingTo("92300.00");

        // --- Parse: DATEV-EXTF Buchungsstapel ---
        List<BuchungRecord> extf = new DatevExtfParser().parse(sample("EXTF_Buchungsstapel_2025-01.csv"));
        assertThat(extf).hasSize(4);
        // WJ-Beginn (20250101) + Belegdatum TTMM (1501/3101) -> Monat 2025-01 rekonstruiert
        assertThat(extf).allSatisfy(r -> assertThat(r.monat()).isEqualTo("2025-01"));
        // Soll/Haben-Kennzeichen korrekt übernommen
        assertThat(extf.get(0).sollHaben()).isEqualTo("H"); // Umsatz 8000
        assertThat(extf.get(1).sollHaben()).isEqualTo("S"); // Wareneinsatz 4000

        // --- Load: echtes Postgres via Testcontainers ---
        BuchungJdbcLoader loader = new BuchungJdbcLoader();
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {

            loader.createSchema(conn);
            int loadedCsv = loader.load(conn, csv);
            int loadedExtf = loader.load(conn, extf);
            assertThat(loadedCsv).isEqualTo(6);
            assertThat(loadedExtf).isEqualTo(4);

            assertThat(count(conn)).isEqualTo(10);

            // Golden Values Jan 2025 (Excel 02_BWA_Import) — beide Quellen reproduzieren dieselben Beträge
            assertThat(sum(conn, "BWA-CSV", "2025-01", "8000")).isEqualByComparingTo("92300.00");
            assertThat(sum(conn, "DATEV-EXTF", "2025-01", "8000")).isEqualByComparingTo("92300.00");
            assertThat(sum(conn, "DATEV-EXTF", "2025-01", "4000")).isEqualByComparingTo("31000.00");
            assertThat(sum(conn, "DATEV-EXTF", "2025-01", "4200")).isEqualByComparingTo("22000.00");
            assertThat(sum(conn, "DATEV-EXTF", "2025-01", "4300")).isEqualByComparingTo("17270.00");

            // EXTF-Gesamtsumme Januar
            assertThat(sumMonth(conn, "DATEV-EXTF", "2025-01")).isEqualByComparingTo("162570.00");
        }
    }

    private long count(Connection conn) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM buchung")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private BigDecimal sum(Connection conn, String quelle, String monat, String konto) throws Exception {
        String sql = "SELECT COALESCE(SUM(betrag),0) FROM buchung "
                + "WHERE quelle='" + quelle + "' AND monat='" + monat + "' AND konto='" + konto + "'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private BigDecimal sumMonth(Connection conn, String quelle, String monat) throws Exception {
        String sql = "SELECT COALESCE(SUM(betrag),0) FROM buchung "
                + "WHERE quelle='" + quelle + "' AND monat='" + monat + "'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}

package com.bwa.poc.datev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Lädt normalisierte Buchungszeilen nach PostgreSQL. Beweist im PoC den End-to-End-Pfad
 * Datei -> Parser -> Datenbank mit korrektem numeric-Typ (BigDecimal) für Geldbeträge.
 */
public class BuchungJdbcLoader {

    public void createSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS buchung (
                    id            BIGSERIAL PRIMARY KEY,
                    quelle        TEXT        NOT NULL,
                    monat         TEXT        NOT NULL,
                    mandant       TEXT        NOT NULL,
                    konto         TEXT        NOT NULL,
                    bezeichnung   TEXT,
                    bwa_gruppe    TEXT,
                    soll_haben    TEXT,
                    betrag        NUMERIC(15,2) NOT NULL,
                    kostenstelle  TEXT
                )
                """);
        }
    }

    public int load(Connection conn, List<BuchungRecord> rows) throws SQLException {
        String sql = """
            INSERT INTO buchung
                (quelle, monat, mandant, konto, bezeichnung, bwa_gruppe, soll_haben, betrag, kostenstelle)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BuchungRecord r : rows) {
                ps.setString(1, r.quelle());
                ps.setString(2, r.monat());
                ps.setString(3, r.mandant());
                ps.setString(4, r.konto());
                ps.setString(5, r.bezeichnung());
                ps.setString(6, r.bwaGruppe());
                ps.setString(7, r.sollHaben());
                ps.setBigDecimal(8, r.betrag());
                ps.setString(9, r.kostenstelle());
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            return counts.length;
        }
    }
}

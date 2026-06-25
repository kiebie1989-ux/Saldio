package com.bwa.controlling.imports;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit-Tests des gehärteten DATEV-EXTF-Parsers (kein DB-Kontext nötig). */
class DatevExtfParserTest {

    private final DatevExtfParser parser = new DatevExtfParser();

    private static final String META =
            "\"EXTF\";700;21;\"Buchungsstapel\";7;20240201;;;;;1001;73841;20240101;4;20240101;20241231;\"Test\"";

    private ByteArrayInputStream bytes(String inhalt, Charset charset) {
        return new ByteArrayInputStream(inhalt.replace("\n", "\r\n").getBytes(charset));
    }

    @Test
    void parstStandardBuchungen() {
        String inhalt = META + "\n"
                + "Umsatz (ohne Soll/Haben-Kz);Soll/Haben-Kennzeichen;Konto;Belegdatum;Buchungstext\n"
                + "92.300,00;H;8000;1503;\"Umsatz März\"\n"
                + "31.000,00;S;3000;1503;\"Wareneinkauf März\"\n";

        List<RohBuchung> r = parser.parse(bytes(inhalt, Charset.forName("windows-1252")));

        assertThat(r).hasSize(2);
        assertThat(r.get(0).monat()).isEqualTo("2024-03"); // WJ-Beginn 2024 + TTMM 1503
        assertThat(r.get(0).konto()).isEqualTo("8000");
        assertThat(r.get(0).sollHaben()).isEqualTo("H");
        assertThat(r.get(0).betrag()).isEqualByComparingTo("92300.00");
    }

    @Test
    void istUnabhaengigVonDerSpaltenreihenfolge() {
        // Andere Reihenfolge der Buchungsspalten -> namensbasiertes Mapping muss trotzdem greifen.
        String inhalt = META + "\n"
                + "Konto;Belegdatum;Buchungstext;Umsatz (ohne Soll/Haben-Kz);Soll/Haben-Kennzeichen\n"
                + "4100;1503;\"Lohn\";22.000,00;S\n";

        List<RohBuchung> r = parser.parse(bytes(inhalt, Charset.forName("windows-1252")));

        assertThat(r).hasSize(1);
        assertThat(r.get(0).konto()).isEqualTo("4100");
        assertThat(r.get(0).betrag()).isEqualByComparingTo("22000.00");
        assertThat(r.get(0).sollHaben()).isEqualTo("S");
    }

    @Test
    void verarbeitetUtf8MitBom() {
        String inhalt = "﻿" + META + "\n"
                + "Umsatz (ohne Soll/Haben-Kz);Soll/Haben-Kennzeichen;Konto;Belegdatum;Buchungstext\n"
                + "10,00;S;4210;0103;\"Miete für Büro\"\n";

        List<RohBuchung> r = parser.parse(bytes(inhalt, StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        assertThat(r).hasSize(1);
        assertThat(r.get(0).bezeichnung()).isEqualTo("Miete für Büro"); // Umlaute korrekt
        assertThat(r.get(0).monat()).isEqualTo("2024-03");
    }

    @Test
    void lehntFalscheDatenkategorieAb() {
        String inhalt = "\"EXTF\";700;16;\"Debitoren\";5;;;;;;1001;73841;20240101;4;;;\n"
                + "Konto;Name\n1000;Test\n";

        assertThatThrownBy(() -> parser.parse(bytes(inhalt, StandardCharsets.UTF_8), StandardCharsets.UTF_8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Datenkategorie");
    }

    @Test
    void lehntFremdesFormatAb() {
        String inhalt = "\"XXXX\";700;21;\"Buchungsstapel\";7;;;;;;1001;73841;20240101;4;;;\n"
                + "Konto\n1000\n";

        assertThatThrownBy(() -> parser.parse(bytes(inhalt, StandardCharsets.UTF_8), StandardCharsets.UTF_8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format-Kennzeichen");
    }
}

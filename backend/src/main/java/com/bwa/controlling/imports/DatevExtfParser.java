package com.bwa.controlling.imports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser des DATEV-EXTF-Buchungsstapels (Datenkategorie 21). Eigenimplementierung (kein OSS).
 *
 * Aufbau:
 *   Zeile 0 = Metazeile (Format-KZ EXTF/DTVF, Version, Datenkategorie, ..., WJ-Beginn, ...)
 *   Zeile 1 = Spaltenüberschriften (Reihenfolge versionsabhängig!)
 *   Zeile 2+ = Buchungssätze
 *
 * Robustheit:
 *  - Buchungsfelder werden über die Spaltenüberschrift (Name) adressiert, nicht über feste Indizes
 *    -> überlebt Versionsunterschiede in der Spaltenreihenfolge des Buchungsstapels.
 *  - Format-KZ (EXTF/DTVF) und Datenkategorie (21) werden validiert.
 *  - UTF-8-BOM wird entfernt; Encoding ist konfigurierbar (DATEV: Windows-1252, neuere: UTF-8).
 *  - Belegdatum kommt als TTMM; das Jahr wird aus dem Wirtschaftsjahr-Beginn der Metazeile rekonstruiert.
 *
 * Hinweis: Die kanonische Spaltenliste je Formatversion ist proprietäre, zugangsbeschränkte
 * DATEV-Doku (developer.datev.de). Durch das namensbasierte Spalten-Mapping ist der Parser
 * unabhängig von der exakten Reihenfolge; die hier erwarteten Spaltennamen entsprechen dem
 * publizierten EXTF-Buchungsstapel-Format und sollten bei Bedarf gegen die offizielle
 * Schnittstellenbeschreibung abgeglichen werden.
 */
@Component
public class DatevExtfParser {

    private static final Charset DATEV_CHARSET = Charset.forName("windows-1252");
    private static final int META_IDX_FORMAT_KZ = 0;
    private static final int META_IDX_DATENKATEGORIE = 2;
    private static final int META_IDX_WJ_BEGINN = 12;
    private static final String DATENKATEGORIE_BUCHUNGSSTAPEL = "21";

    private static final String COL_UMSATZ = "Umsatz (ohne Soll/Haben-Kz)";
    private static final String COL_SOLL_HABEN = "Soll/Haben-Kennzeichen";
    private static final String COL_KONTO = "Konto";
    private static final String COL_BELEGDATUM = "Belegdatum";
    private static final String COL_BUCHUNGSTEXT = "Buchungstext";

    public List<RohBuchung> parse(InputStream in) {
        return parse(in, DATEV_CHARSET);
    }

    public List<RohBuchung> parse(InputStream in, Charset charset) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .get();

        try (Reader reader = new InputStreamReader(in, charset);
             CSVParser parser = CSVParser.parse(entferneBom(reader), format)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() < 2) {
                throw new IllegalArgumentException("EXTF-Datei unvollständig: Metazeile/Spaltenkopf fehlen");
            }

            CSVRecord meta = records.get(0);
            validiereMetazeile(meta);

            String wjBeginn = meta.get(META_IDX_WJ_BEGINN);
            if (wjBeginn == null || wjBeginn.length() < 4) {
                throw new IllegalArgumentException("EXTF-Metazeile ohne gültigen Wirtschaftsjahr-Beginn (Feld 13)");
            }
            String jahr = wjBeginn.substring(0, 4);
            String mandant = meta.size() > 11 ? meta.get(11) : "";

            Map<String, Integer> colIndex = buildColumnIndex(records.get(1));

            List<RohBuchung> result = new ArrayList<>();
            for (int i = 2; i < records.size(); i++) {
                CSVRecord row = records.get(i);
                if (istLeer(row)) {
                    continue;
                }
                String monat = jahr + "-" + monthFromTtmm(value(row, colIndex, COL_BELEGDATUM));
                result.add(new RohBuchung(
                        "DATEV-EXTF",
                        monat,
                        mandant,
                        value(row, colIndex, COL_KONTO),
                        value(row, colIndex, COL_BUCHUNGSTEXT),
                        null,
                        value(row, colIndex, COL_SOLL_HABEN),
                        GermanAmount.parse(value(row, colIndex, COL_UMSATZ)),
                        ""
                ));
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("EXTF-Datei konnte nicht gelesen werden", e);
        }
    }

    private static void validiereMetazeile(CSVRecord meta) {
        String formatKz = meta.size() > META_IDX_FORMAT_KZ ? meta.get(META_IDX_FORMAT_KZ).trim() : "";
        if (!formatKz.equals("EXTF") && !formatKz.equals("DTVF")) {
            throw new IllegalArgumentException(
                    "Keine DATEV-EXTF/DTVF-Datei (Format-Kennzeichen: '" + formatKz + "')");
        }
        String kategorie = meta.size() > META_IDX_DATENKATEGORIE ? meta.get(META_IDX_DATENKATEGORIE).trim() : "";
        if (!kategorie.equals(DATENKATEGORIE_BUCHUNGSSTAPEL)) {
            throw new IllegalArgumentException(
                    "Nicht unterstützte DATEV-Datenkategorie '" + kategorie + "' (erwartet 21 = Buchungsstapel)");
        }
    }

    /** Entfernt ein etwaiges UTF-8-BOM am Dateianfang. */
    private static Reader entferneBom(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        br.mark(1);
        int erstes = br.read();
        if (erstes != 0xFEFF) {
            br.reset();
        }
        return br;
    }

    private static Map<String, Integer> buildColumnIndex(CSVRecord headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            map.put(headerRow.get(i).trim(), i);
        }
        return map;
    }

    private static boolean istLeer(CSVRecord row) {
        for (String f : row) {
            if (f != null && !f.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String value(CSVRecord row, Map<String, Integer> colIndex, String column) {
        Integer idx = colIndex.get(column);
        if (idx == null) {
            throw new IllegalStateException("Spalte fehlt im EXTF-Buchungsstapel: '" + column + "'");
        }
        return idx < row.size() ? row.get(idx) : "";
    }

    /** TTMM -> MM (Belegdatum hat in DATEV das Format Tag+Monat ohne Jahr). */
    private static String monthFromTtmm(String ttmm) {
        if (ttmm == null || ttmm.length() < 3) {
            throw new IllegalArgumentException("Ungültiges TTMM-Belegdatum: '" + ttmm + "'");
        }
        String padded = ttmm.length() == 3 ? "0" + ttmm : ttmm;
        String mm = padded.substring(2, 4);
        int monat = Integer.parseInt(mm);
        if (monat < 1 || monat > 12) {
            throw new IllegalArgumentException("Ungültiger Monat im TTMM-Belegdatum: '" + ttmm + "'");
        }
        return mm;
    }

    // Referenz: UTF-8-Charset für Aufrufer, die explizit UTF-8 erzwingen wollen.
    public static Charset utf8() {
        return StandardCharsets.UTF_8;
    }
}

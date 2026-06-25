package com.bwa.poc.datev;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eigenimplementierung des DATEV-EXTF-Buchungsstapel-Parsers (Datenkategorie 21).
 * Kein brauchbares OSS vorhanden (Phase-0), daher Eigenbau.
 *
 * Aufbau der Datei:
 *   Zeile 0 = Metazeile (Format-KZ, Version, Datenkategorie, ..., WJ-Beginn, ...)
 *   Zeile 1 = Spaltenüberschriften (Reihenfolge versionsabhängig!)
 *   Zeile 2+ = Buchungssätze
 *
 * Robustheit: Buchungsfelder werden über die Spaltenüberschrift (Name) adressiert,
 * nicht über feste Indizes — so überleben wir Versionsunterschiede im Buchungsstapel.
 * Das Belegdatum kommt als TTMM; das Jahr wird aus dem Wirtschaftsjahr-Beginn (Metazeile) rekonstruiert.
 */
public class DatevExtfParser {

    private static final Charset DATEV_CHARSET = Charset.forName("windows-1252");

    /** 0-basierte Position des Wirtschaftsjahr-Beginns (YYYYMMDD) in der Metazeile laut DATEV-Spec. */
    private static final int META_IDX_WJ_BEGINN = 12;

    private static final String COL_UMSATZ = "Umsatz (ohne Soll/Haben-Kz)";
    private static final String COL_SOLL_HABEN = "Soll/Haben-Kennzeichen";
    private static final String COL_KONTO = "Konto";
    private static final String COL_BELEGDATUM = "Belegdatum";
    private static final String COL_BUCHUNGSTEXT = "Buchungstext";

    private final Charset charset;

    public DatevExtfParser() {
        this(DATEV_CHARSET);
    }

    public DatevExtfParser(Charset charset) {
        this.charset = charset;
    }

    public List<BuchungRecord> parse(InputStream in) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .get();

        try (Reader reader = new InputStreamReader(in, charset);
             CSVParser parser = CSVParser.parse(reader, format)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() < 2) {
                throw new IllegalArgumentException("EXTF-Datei unvollständig: Metazeile/Spaltenkopf fehlen");
            }

            CSVRecord meta = records.get(0);
            String wjBeginn = meta.get(META_IDX_WJ_BEGINN);          // z.B. 20250101
            String jahr = wjBeginn.substring(0, 4);
            String mandant = meta.get(11);                            // Mandantennummer aus Metazeile

            Map<String, Integer> colIndex = buildColumnIndex(records.get(1));

            List<BuchungRecord> result = new ArrayList<>();
            for (int i = 2; i < records.size(); i++) {
                CSVRecord row = records.get(i);
                String umsatz = value(row, colIndex, COL_UMSATZ);
                String sollHaben = value(row, colIndex, COL_SOLL_HABEN);
                String konto = value(row, colIndex, COL_KONTO);
                String belegdatum = value(row, colIndex, COL_BELEGDATUM); // TTMM
                String buchungstext = value(row, colIndex, COL_BUCHUNGSTEXT);

                String monat = jahr + "-" + monthFromTtmm(belegdatum);

                result.add(new BuchungRecord(
                        "DATEV-EXTF",
                        monat,
                        mandant,
                        konto,
                        buchungstext,
                        "",
                        sollHaben,
                        GermanAmount.parse(umsatz),
                        ""
                ));
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("EXTF-Datei konnte nicht gelesen werden", e);
        }
    }

    private static Map<String, Integer> buildColumnIndex(CSVRecord headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            map.put(headerRow.get(i).trim(), i);
        }
        return map;
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
        String padded = ttmm.length() == 3 ? "0" + ttmm : ttmm; // z.B. "101" -> "0101"
        return padded.substring(2, 4);
    }
}

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
import java.util.List;

/**
 * Parser für das vereinfachte BWA-CSV (Layout wie Excel-Blatt 02_BWA_Import):
 * Monat;Mandant;Konto;Kontobezeichnung;BWA-Gruppe;Betrag;Kostenstelle
 *
 * DATEV-typisch: Semikolon-getrennt, Windows-1252-Encoding, deutsches Dezimalkomma.
 */
public class SimpleBwaCsvParser {

    private static final Charset DATEV_CHARSET = Charset.forName("windows-1252");

    private final Charset charset;

    public SimpleBwaCsvParser() {
        this(DATEV_CHARSET);
    }

    public SimpleBwaCsvParser(Charset charset) {
        this.charset = charset;
    }

    public List<BuchungRecord> parse(InputStream in) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        List<BuchungRecord> result = new ArrayList<>();
        try (Reader reader = new InputStreamReader(in, charset);
             CSVParser parser = CSVParser.parse(reader, format)) {
            for (CSVRecord r : parser) {
                result.add(new BuchungRecord(
                        "BWA-CSV",
                        r.get("Monat"),
                        r.get("Mandant"),
                        r.get("Konto"),
                        r.get("Kontobezeichnung"),
                        r.get("BWA-Gruppe"),
                        "",
                        GermanAmount.parse(r.get("Betrag")),
                        r.get("Kostenstelle")
                ));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("BWA-CSV konnte nicht gelesen werden", e);
        }
        return result;
    }
}

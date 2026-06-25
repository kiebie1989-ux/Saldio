package com.bwa.controlling.imports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser für das vereinfachte Saldenlisten-CSV:
 * Monat;Mandant;Konto;Kontobezeichnung;Betrag;Kostenstelle
 * (Semikolon, Windows-1252, deutsches Dezimalkomma).
 *
 * BWA-Gruppe und GuV/Bilanz-Position werden NICHT aus der Datei gelesen, sondern beim Import
 * über das Kontenmapping ergänzt (entspricht dem SVERWEIS im Excel-Blatt 01_Saldenlisten_Import).
 */
@Component
public class BwaCsvParser {

    private static final Charset DATEV_CHARSET = Charset.forName("windows-1252");

    public List<RohBuchung> parse(InputStream in) {
        return parse(in, DATEV_CHARSET);
    }

    public List<RohBuchung> parse(InputStream in, Charset charset) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        List<RohBuchung> result = new ArrayList<>();
        try (Reader reader = new InputStreamReader(in, charset);
             CSVParser parser = CSVParser.parse(reader, format)) {
            for (CSVRecord r : parser) {
                result.add(new RohBuchung(
                        "BWA-CSV",
                        r.get("Monat"),
                        r.get("Mandant"),
                        r.get("Konto"),
                        r.get("Kontobezeichnung"),
                        null,
                        null,
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

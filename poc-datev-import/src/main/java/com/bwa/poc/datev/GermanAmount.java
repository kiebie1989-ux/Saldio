package com.bwa.poc.datev;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Parser für deutsche Dezimalzahlen aus DATEV-Dateien: Tausenderpunkt + Dezimalkomma
 * (z.B. "92.300,00" -> 92300.00). Keine CSV-Library kann das von Haus aus.
 */
public final class GermanAmount {

    private GermanAmount() {}

    public static BigDecimal parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
        format.setParseBigDecimal(true);
        try {
            return (BigDecimal) format.parse(raw.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Kein gültiger deutscher Betrag: '" + raw + "'", e);
        }
    }
}

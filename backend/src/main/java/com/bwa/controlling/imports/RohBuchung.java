package com.bwa.controlling.imports;

import java.math.BigDecimal;

/**
 * Rohzeile aus einem Parser, vor der Anreicherung durch das Kontenmapping.
 *
 * @param quelle       Herkunft ("BWA-CSV" | "DATEV-EXTF")
 * @param monat        JJJJ-MM
 * @param mandant      Mandant (Name bzw. DATEV-Mandantennummer)
 * @param konto        Sachkonto (SKR03/SKR04)
 * @param bezeichnung  Kontobezeichnung / Buchungstext
 * @param bwaGruppe    BWA-Gruppe falls in der Quelle vorhanden (sonst null -> aus Mapping)
 * @param sollHaben    "S"/"H" (nur EXTF)
 * @param betrag       Betrag
 * @param kostenstelle Kostenstelle (optional)
 */
public record RohBuchung(
        String quelle,
        String monat,
        String mandant,
        String konto,
        String bezeichnung,
        String bwaGruppe,
        String sollHaben,
        BigDecimal betrag,
        String kostenstelle
) {}

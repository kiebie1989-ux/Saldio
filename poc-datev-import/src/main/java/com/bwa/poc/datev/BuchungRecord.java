package com.bwa.poc.datev;

import java.math.BigDecimal;

/**
 * Normalisierte Buchungszeile — gemeinsames Ergebnis beider Importwege
 * (vereinfachtes BWA-CSV und DATEV-EXTF-Buchungsstapel).
 *
 * @param quelle       Herkunft ("BWA-CSV" | "DATEV-EXTF")
 * @param monat        Buchungsmonat im Format JJJJ-MM
 * @param mandant      Mandantenname
 * @param konto        Sachkonto (SKR03/SKR04)
 * @param bezeichnung  Kontobezeichnung / Buchungstext
 * @param bwaGruppe    BWA-Gruppe (nur CSV; bei EXTF leer, wird später per Kontenrahmen gemappt)
 * @param sollHaben    "S" oder "H" (nur EXTF; bei CSV leer)
 * @param betrag       Betrag als BigDecimal (positiver Umsatzbetrag)
 * @param kostenstelle Kostenstelle (optional)
 */
public record BuchungRecord(
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

package com.bwa.controlling.engine;

import java.math.BigDecimal;

/** Aggregierte Summe je Monat und BWA-Gruppe (Projektion der GROUP-BY-Query). */
public interface GruppenSumme {
    String getMonat();
    String getGruppe();
    BigDecimal getSumme();
}

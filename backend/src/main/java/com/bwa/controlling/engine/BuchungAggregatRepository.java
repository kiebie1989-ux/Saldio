package com.bwa.controlling.engine;

import com.bwa.controlling.imports.Buchung;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Aggregation der Buchungen als SUMIFS-Äquivalent — die Summenbildung läuft in PostgreSQL
 * (GROUP BY), nicht in Java. Gefiltert nach Mandant und Jahr, gruppiert nach Monat + BWA-Gruppe.
 */
public interface BuchungAggregatRepository extends Repository<Buchung, Long> {

    @Query("""
            select b.monat as monat, b.bwaGruppe as gruppe, sum(b.betrag) as summe
            from Buchung b
            where b.mandant = :mandant and b.monat like concat(:jahr, '-%')
            group by b.monat, b.bwaGruppe
            """)
    List<GruppenSumme> aggregiere(@Param("mandant") String mandant, @Param("jahr") String jahr);
}

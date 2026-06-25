package com.bwa.controlling.stammdaten;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring-Data-Repositories der Stammdaten. */
public final class StammdatenRepositories {
    private StammdatenRepositories() {}
}

interface KontenrahmenRepository extends JpaRepository<Kontenrahmen, Long> {
    List<Kontenrahmen> findByAktivTrueOrderBySkr03Asc();
    List<Kontenrahmen> findBySkr03(String skr03);
    List<Kontenrahmen> findBySkr04(String skr04);
}

interface MandantRepository extends JpaRepository<Mandant, Long> {
    List<Mandant> findAllByOrderByNameAsc();
    List<Mandant> findByInKumulierungTrueOrderByNameAsc();
    List<Mandant> findByImFinalberichtTrueOrderByNameAsc();
}

interface EinstellungRepository extends JpaRepository<Einstellung, String> {
}

interface MitarbeiterRepository extends JpaRepository<Mitarbeiter, Long> {
    List<Mitarbeiter> findAllByOrderByPersonalnummerAsc();
    List<Mitarbeiter> findByMandantOrderByKostenstelleAsc(String mandant);
    long countByMandant(String mandant);
}

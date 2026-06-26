package com.bwa.controlling.stammdaten;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Öffentliche Abfragen auf Stammdaten, die andere Module (z.B. die Engine) benötigen. */
@Service
public class StammdatenService {

    private final MandantRepository mandanten;
    private final MitarbeiterRepository mitarbeiter;
    private final EinstellungRepository einstellungen;

    public StammdatenService(MandantRepository mandanten, MitarbeiterRepository mitarbeiter,
                             EinstellungRepository einstellungen) {
        this.mandanten = mandanten;
        this.mitarbeiter = mitarbeiter;
        this.einstellungen = einstellungen;
    }

    public long mitarbeiterAnzahl(String mandant) {
        return mitarbeiter.countByMandant(mandant);
    }

    public List<Mitarbeiter> mitarbeiterVon(String mandant) {
        return mitarbeiter.findByMandantOrderByKostenstelleAsc(mandant);
    }

    /** Alle Mandantennamen. */
    public List<String> alleMandantennamen() {
        return mandanten.findAllByOrderByNameAsc().stream().map(Mandant::getName).toList();
    }

    /** Mandantennamen für die Kumulierung (Flag in_kumulierung). */
    public List<String> kumulierteMandanten() {
        return mandanten.findByInKumulierungTrueOrderByNameAsc().stream().map(Mandant::getName).toList();
    }

    /** Mandantennamen für den Finalbericht (Flag im_finalbericht). */
    public List<String> finaleMandanten() {
        return mandanten.findByImFinalberichtTrueOrderByNameAsc().stream().map(Mandant::getName).toList();
    }

    /** Der in den Einstellungen gewählte aktive (Fokus-)Mandant. */
    public Optional<String> aktiverMandant() {
        return einstellung("Aktiver Mandant");
    }

    /** Löst eine DATEV-Mandantennummer (aus EXTF) zum Mandantennamen auf. */
    public Optional<String> mandantNameFuerDatevNr(String datevNr) {
        if (datevNr == null || datevNr.isBlank()) {
            return Optional.empty();
        }
        return mandanten.findByDatevMandantennr(datevNr.trim()).map(Mandant::getName);
    }

    public Optional<String> einstellung(String schluessel) {
        return einstellungen.findById(schluessel).map(Einstellung::getWert);
    }

    /** Liest einen numerischen Einstellungswert (z.B. Zielwert), falls vorhanden und gültig. */
    public Optional<BigDecimal> zielwert(String schluessel) {
        return einstellung(schluessel).flatMap(w -> {
            try {
                return Optional.of(new BigDecimal(w.trim().replace(",", ".")));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }
}

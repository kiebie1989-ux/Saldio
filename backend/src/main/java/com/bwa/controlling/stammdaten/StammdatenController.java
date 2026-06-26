package com.bwa.controlling.stammdaten;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Lesezugriff auf die Stammdaten (Kontenrahmen, Mandanten, Einstellungen, Mitarbeiter). */
@RestController
@RequestMapping("/api")
public class StammdatenController {

    /** Arbeitgeberanteil auf den Bruttolohn (vereinfacht 20 %, wie im Excel-Blatt 03_Mitarbeiter). */
    private static final BigDecimal AG_ANTEIL_SATZ = new BigDecimal("0.20");

    private final KontenrahmenRepository kontenrahmen;
    private final MandantRepository mandanten;
    private final EinstellungRepository einstellungen;
    private final MitarbeiterRepository mitarbeiter;
    private final com.bwa.controlling.benutzer.MandantenZugriffService zugriff;
    private final com.bwa.controlling.revision.AuditService audit;

    public StammdatenController(KontenrahmenRepository kontenrahmen,
                                MandantRepository mandanten,
                                EinstellungRepository einstellungen,
                                MitarbeiterRepository mitarbeiter,
                                com.bwa.controlling.benutzer.MandantenZugriffService zugriff,
                                com.bwa.controlling.revision.AuditService audit) {
        this.kontenrahmen = kontenrahmen;
        this.mandanten = mandanten;
        this.einstellungen = einstellungen;
        this.mitarbeiter = mitarbeiter;
        this.zugriff = zugriff;
        this.audit = audit;
    }

    @GetMapping("/kontenrahmen")
    public List<Kontenrahmen> kontenrahmen() {
        return kontenrahmen.findAll();
    }

    @GetMapping("/mandanten")
    public List<Mandant> mandanten() {
        var erlaubt = zugriff.erlaubteMandanten();
        return mandanten.findAllByOrderByNameAsc().stream()
                .filter(m -> erlaubt.contains(m.getName()))
                .toList();
    }

    @org.springframework.web.bind.annotation.PostMapping("/mandanten")
    public Mandant anlegen(@RequestBody MandantEingabe e) {
        Mandant m = new Mandant();
        uebernehmen(m, e);
        Mandant gespeichert = mandanten.save(m);
        audit.protokolliere("MANDANT_ANLEGEN", "mandant", e.name(), "DATEV-Nr=" + e.datevMandantennr());
        return gespeichert;
    }

    @PutMapping("/mandanten/{id}")
    public Mandant bearbeiten(@org.springframework.web.bind.annotation.PathVariable Long id,
                              @RequestBody MandantEingabe e) {
        Mandant m = mandanten.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Mandant nicht gefunden: " + id));
        uebernehmen(m, e);
        Mandant gespeichert = mandanten.save(m);
        audit.protokolliere("MANDANT_AENDERN", "mandant", String.valueOf(id), e.name());
        return gespeichert;
    }

    private static void uebernehmen(Mandant m, MandantEingabe e) {
        m.setName(e.name());
        m.setStatus(e.status() == null ? "AKTIV" : e.status());
        m.setImEinzelbericht(e.imEinzelbericht());
        m.setInKumulierung(e.inKumulierung());
        m.setImFinalbericht(e.imFinalbericht());
        m.setTyp(e.typ());
        m.setBemerkung(e.bemerkung());
        m.setDatevMandantennr(e.datevMandantennr());
        m.setDatevBeraternr(e.datevBeraternr());
    }

    public record MandantEingabe(String name, String status, boolean imEinzelbericht, boolean inKumulierung,
                                 boolean imFinalbericht, String typ, String bemerkung,
                                 String datevMandantennr, String datevBeraternr) {}

    @GetMapping("/einstellungen")
    public List<Einstellung> einstellungen() {
        return einstellungen.findAll();
    }

    @PutMapping("/einstellungen")
    public Einstellung aktualisiereEinstellung(@RequestBody EinstellungUpdate body) {
        Einstellung e = einstellungen.findById(body.schluessel())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Einstellung nicht gefunden: " + body.schluessel()));
        e.setWert(body.wert());
        Einstellung gespeichert = einstellungen.save(e);
        audit.protokolliere("EINSTELLUNG_AENDERN", "einstellung", e.getSchluessel(),
                "neuer Wert: " + body.wert());
        return gespeichert;
    }

    // Schlüssel im Body statt im Pfad: die Schlüssel enthalten Sonderzeichen (%, Leerzeichen),
    // die in URL-Pfaden zu Firewall-/Encoding-Problemen führen.
    public record EinstellungUpdate(String schluessel, String wert) {}

    @GetMapping("/mitarbeiter")
    public List<MitarbeiterDto> mitarbeiter() {
        var erlaubt = zugriff.erlaubteMandanten();
        return mitarbeiter.findAllByOrderByPersonalnummerAsc().stream()
                .filter(m -> erlaubt.contains(m.getMandant()))
                .map(StammdatenController::toDto)
                .toList();
    }

    private static MitarbeiterDto toDto(Mitarbeiter m) {
        BigDecimal euroProStd = m.getStundenProMonat().signum() == 0
                ? BigDecimal.ZERO
                : m.getMonatslohn().divide(m.getStundenProMonat(), 2, RoundingMode.HALF_UP);
        BigDecimal agAnteil = m.getMonatslohn().multiply(AG_ANTEIL_SATZ).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gesamtkosten = m.getMonatslohn().add(agAnteil).setScale(2, RoundingMode.HALF_UP);
        return new MitarbeiterDto(
                m.getPersonalnummer(), m.getName(), m.getMandant(), m.getKostenstelle(), m.getTeam(),
                m.getMonatslohn(), m.getStundenProMonat(), euroProStd, agAnteil, gesamtkosten);
    }

    public record MitarbeiterDto(
            String personalnummer, String name, String mandant, String kostenstelle, String team,
            BigDecimal monatslohn, BigDecimal stundenProMonat,
            BigDecimal euroProStunde, BigDecimal agAnteil, BigDecimal gesamtkosten) {}
}

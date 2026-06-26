package com.bwa.controlling.imports;

import com.bwa.controlling.revision.AuditService;
import com.bwa.controlling.revision.FestschreibungService;
import com.bwa.controlling.revision.SicherheitsKontext;
import com.bwa.controlling.stammdaten.KontenmappingService;
import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Importiert Rohbuchungen (CSV oder EXTF), reichert sie an (BWA-Gruppe, GuV/Bilanz-Position,
 * vorzeichenrichtiger Betrag) und persistiert sie samt {@link ImportBatch}-Status.
 *
 * Phase A: EXTF-Mandantennummer wird zum Mandantennamen aufgelöst; Soll/Haben wird über
 * {@link BetragsLogik} in einen vorzeichenrichtigen Betrag übersetzt; identische Dateien werden
 * per Inhalts-Hash als Doppel-Import abgelehnt.
 */
@Service
public class ImportService {

    public enum Quelle { CSV, EXTF }

    private final BwaCsvParser csvParser;
    private final DatevExtfParser extfParser;
    private final KontenmappingService mapping;
    private final StammdatenService stammdaten;
    private final BuchungRepository buchungRepository;
    private final ImportBatchRepository batchRepository;
    private final AuditService audit;
    private final SicherheitsKontext kontext;
    private final FestschreibungService festschreibung;

    public ImportService(BwaCsvParser csvParser,
                         DatevExtfParser extfParser,
                         KontenmappingService mapping,
                         StammdatenService stammdaten,
                         BuchungRepository buchungRepository,
                         ImportBatchRepository batchRepository,
                         AuditService audit,
                         SicherheitsKontext kontext,
                         FestschreibungService festschreibung) {
        this.csvParser = csvParser;
        this.extfParser = extfParser;
        this.mapping = mapping;
        this.stammdaten = stammdaten;
        this.buchungRepository = buchungRepository;
        this.batchRepository = batchRepository;
        this.audit = audit;
        this.kontext = kontext;
        this.festschreibung = festschreibung;
    }

    @Transactional
    public ImportBatch importieren(Quelle quelle, String dateiname, InputStream data) {
        byte[] inhalt = lesen(data);
        String hash = sha256(inhalt);

        // Doppel-Import-Schutz: identische Datei wurde bereits importiert.
        batchRepository.findFirstByDateiHash(hash).ifPresent(vorhanden -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Diese Datei wurde bereits importiert (Stapel #" + vorhanden.getId() + ")");
        });

        List<RohBuchung> rohzeilen = switch (quelle) {
            case CSV -> csvParser.parse(new ByteArrayInputStream(inhalt));
            case EXTF -> extfParser.parse(new ByteArrayInputStream(inhalt));
        };

        ImportBatch batch = new ImportBatch(dateiname, quelle.name());
        batch.setDateiHash(hash);
        batch.setErstelltVon(kontext.sub());
        batch = batchRepository.save(batch);

        int ok = 0;
        int warnung = 0;
        List<Buchung> buchungen = new ArrayList<>(rohzeilen.size());
        for (RohBuchung roh : rohzeilen) {
            Buchung b = new Buchung();
            b.setImportBatchId(batch.getId());
            b.setQuelle(roh.quelle());
            b.setMonat(roh.monat());
            b.setKonto(roh.konto());
            b.setBezeichnung(roh.bezeichnung());
            b.setSollHaben(roh.sollHaben());
            b.setKostenstelle(roh.kostenstelle());

            String status = "OK";

            // 1) Mandant: bei EXTF ist es die DATEV-Mandantennummer -> auf Namen auflösen.
            String mandant = roh.mandant();
            if (quelle == Quelle.EXTF) {
                Optional<String> name = stammdaten.mandantNameFuerDatevNr(roh.mandant());
                if (name.isPresent()) {
                    mandant = name.get();
                } else {
                    status = "WARN_MANDANT_UNBEKANNT";
                }
            }
            b.setMandant(mandant);

            // Schreibsperre: in eine festgeschriebene Periode darf nicht mehr gebucht werden.
            if (festschreibung.istFestgeschrieben(mandant, roh.monat())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Periode ist festgeschrieben: " + mandant + " " + roh.monat());
            }

            // 2) Konto-Mapping (BWA-Gruppe, Position, Kontenklasse) + vorzeichenrichtiger Betrag.
            Optional<KontenmappingService.Mapping> m = mapping.resolve(roh.konto());
            if (m.isPresent()) {
                b.setBwaGruppe(roh.bwaGruppe() != null ? roh.bwaGruppe() : m.get().bwaGruppe());
                b.setGuvBilanzPosition(m.get().guvBilanzPosition());
                b.setBetrag(BetragsLogik.vorzeichenrichtig(roh.betrag(), roh.sollHaben(), m.get().kontenklasse()));
            } else {
                b.setBwaGruppe(roh.bwaGruppe());
                b.setBetrag(BetragsLogik.vorzeichenrichtig(roh.betrag(), roh.sollHaben(), null));
                if (status.equals("OK")) {
                    status = "WARN_KONTO_UNBEKANNT";
                }
            }

            b.setStatus(status);
            if (status.equals("OK")) {
                ok++;
            } else {
                warnung++;
            }
            buchungen.add(b);
        }
        buchungRepository.saveAll(buchungen);

        batch.setZeilenGesamt(rohzeilen.size());
        batch.setZeilenOk(ok);
        batch.setZeilenWarnung(warnung);
        batch.setStatus(warnung == 0 ? "OK" : "MIT_WARNUNGEN");
        ImportBatch gespeichert = batchRepository.save(batch);

        audit.protokolliere("IMPORT", "import_batch", String.valueOf(gespeichert.getId()),
                "%s (%s): %d Zeilen, %d OK, %d Warnung".formatted(
                        dateiname, quelle, rohzeilen.size(), ok, warnung));
        return gespeichert;
    }

    /**
     * Storniert einen Import-Stapel durch einen Gegenstapel mit negierten Buchungen.
     * Das Original bleibt unverändert (Unveränderbarkeit). Festgeschriebene Perioden sind gesperrt.
     */
    @Transactional
    public ImportBatch storniere(Long batchId) {
        ImportBatch original = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Import-Stapel nicht gefunden: " + batchId));
        if (batchRepository.findFirstByStorniertBatchId(batchId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stapel #" + batchId + " wurde bereits storniert");
        }
        List<Buchung> originalBuchungen = buchungRepository.findByImportBatchId(batchId);
        for (Buchung b : originalBuchungen) {
            if (festschreibung.istFestgeschrieben(b.getMandant(), b.getMonat())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Periode festgeschrieben, Storno nicht möglich: " + b.getMandant() + " " + b.getMonat());
            }
        }

        ImportBatch storno = new ImportBatch("Storno zu #" + batchId, "STORNO");
        storno.setErstelltVon(kontext.sub());
        storno.setStorniertBatchId(batchId);
        storno = batchRepository.save(storno);

        List<Buchung> gegen = new ArrayList<>(originalBuchungen.size());
        for (Buchung o : originalBuchungen) {
            Buchung b = new Buchung();
            b.setImportBatchId(storno.getId());
            b.setQuelle("STORNO");
            b.setMonat(o.getMonat());
            b.setMandant(o.getMandant());
            b.setKonto(o.getKonto());
            b.setBezeichnung("Storno: " + o.getBezeichnung());
            b.setBwaGruppe(o.getBwaGruppe());
            b.setGuvBilanzPosition(o.getGuvBilanzPosition());
            b.setSollHaben(o.getSollHaben());
            b.setBetrag(o.getBetrag().negate());
            b.setKostenstelle(o.getKostenstelle());
            b.setStatus("STORNO");
            gegen.add(b);
        }
        buchungRepository.saveAll(gegen);

        storno.setZeilenGesamt(gegen.size());
        storno.setZeilenOk(gegen.size());
        storno.setStatus("STORNO");
        ImportBatch gespeichert = batchRepository.save(storno);
        audit.protokolliere("STORNO", "import_batch", String.valueOf(batchId),
                "Gegenstapel #" + gespeichert.getId() + ", " + gegen.size() + " Buchungen negiert");
        return gespeichert;
    }

    private static byte[] lesen(InputStream data) {
        try {
            return data.readAllBytes();
        } catch (java.io.IOException e) {
            throw new UncheckedIOException("Datei konnte nicht gelesen werden", e);
        }
    }

    private static String sha256(byte[] inhalt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(inhalt));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

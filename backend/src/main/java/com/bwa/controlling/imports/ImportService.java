package com.bwa.controlling.imports;

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

    public ImportService(BwaCsvParser csvParser,
                         DatevExtfParser extfParser,
                         KontenmappingService mapping,
                         StammdatenService stammdaten,
                         BuchungRepository buchungRepository,
                         ImportBatchRepository batchRepository) {
        this.csvParser = csvParser;
        this.extfParser = extfParser;
        this.mapping = mapping;
        this.stammdaten = stammdaten;
        this.buchungRepository = buchungRepository;
        this.batchRepository = batchRepository;
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
        return batchRepository.save(batch);
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

package com.bwa.controlling.imports;

import com.bwa.controlling.stammdaten.KontenmappingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Importiert Rohbuchungen (CSV oder EXTF), reichert sie über das Kontenmapping an
 * (BWA-Gruppe + GuV/Bilanz-Position) und persistiert sie samt {@link ImportBatch}-Status.
 */
@Service
public class ImportService {

    public enum Quelle { CSV, EXTF }

    private final BwaCsvParser csvParser;
    private final DatevExtfParser extfParser;
    private final KontenmappingService mapping;
    private final BuchungRepository buchungRepository;
    private final ImportBatchRepository batchRepository;

    public ImportService(BwaCsvParser csvParser,
                         DatevExtfParser extfParser,
                         KontenmappingService mapping,
                         BuchungRepository buchungRepository,
                         ImportBatchRepository batchRepository) {
        this.csvParser = csvParser;
        this.extfParser = extfParser;
        this.mapping = mapping;
        this.buchungRepository = buchungRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional
    public ImportBatch importieren(Quelle quelle, String dateiname, InputStream data) {
        List<RohBuchung> rohzeilen = switch (quelle) {
            case CSV -> csvParser.parse(data);
            case EXTF -> extfParser.parse(data);
        };

        ImportBatch batch = batchRepository.save(new ImportBatch(dateiname, quelle.name()));

        int ok = 0;
        int warnung = 0;
        List<Buchung> buchungen = new ArrayList<>(rohzeilen.size());
        for (RohBuchung roh : rohzeilen) {
            Buchung b = new Buchung();
            b.setImportBatchId(batch.getId());
            b.setQuelle(roh.quelle());
            b.setMonat(roh.monat());
            b.setMandant(roh.mandant());
            b.setKonto(roh.konto());
            b.setBezeichnung(roh.bezeichnung());
            b.setSollHaben(roh.sollHaben());
            b.setBetrag(roh.betrag());
            b.setKostenstelle(roh.kostenstelle());

            Optional<KontenmappingService.Mapping> m = mapping.resolve(roh.konto());
            if (m.isPresent()) {
                b.setBwaGruppe(roh.bwaGruppe() != null ? roh.bwaGruppe() : m.get().bwaGruppe());
                b.setGuvBilanzPosition(m.get().guvBilanzPosition());
                b.setStatus("OK");
                ok++;
            } else {
                b.setBwaGruppe(roh.bwaGruppe());
                b.setStatus("WARN_KONTO_UNBEKANNT");
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
}

package com.bwa.controlling.revision;

import com.bwa.controlling.imports.Buchung;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Prüfbarer GoBD-Export je Mandant/Jahr: alle Buchungen, die Festschreibungs-Hashkette und eine
 * Integritätsprüfung. Dient als Grundlage für die Betriebsprüfung.
 *
 * Hinweis: Dies ist ein vollständiger, verifizierbarer JSON-Export. Ein formaler DSFinV-K-/
 * GDPdU-Export (amtliches Beschreibungsformat) ist ein separates Ausbauthema.
 */
@Service
public class GobdExportService {

    public record GobdExport(
            String mandant, int jahr, OffsetDateTime exportiertAm, String exportiertVon,
            List<Buchung> buchungen, List<Festschreibung> festschreibungen,
            FestschreibungService.PruefErgebnis integritaet) {}

    private final GobdBuchungRepository buchungen;
    private final FestschreibungService festschreibung;
    private final SicherheitsKontext kontext;
    private final AuditService audit;

    public GobdExportService(GobdBuchungRepository buchungen, FestschreibungService festschreibung,
                             SicherheitsKontext kontext, AuditService audit) {
        this.buchungen = buchungen;
        this.festschreibung = festschreibung;
        this.kontext = kontext;
        this.audit = audit;
    }

    @Transactional
    public GobdExport export(String mandant, int jahr) {
        List<Buchung> alle = buchungen.findByMandantAndJahr(mandant, String.valueOf(jahr));
        List<Festschreibung> fest = festschreibung.fuerMandant(mandant);
        FestschreibungService.PruefErgebnis integritaet = festschreibung.pruefe(mandant);
        audit.protokolliere("GOBD_EXPORT", "mandant", mandant, "Jahr " + jahr + ", " + alle.size() + " Buchungen");
        return new GobdExport(mandant, jahr, OffsetDateTime.now(), kontext.sub(), alle, fest, integritaet);
    }
}

interface GobdBuchungRepository extends Repository<Buchung, Long> {
    @Query("select b from Buchung b where b.mandant = :mandant and b.monat like concat(:jahr, '-%') order by b.id")
    List<Buchung> findByMandantAndJahr(String mandant, String jahr);
}

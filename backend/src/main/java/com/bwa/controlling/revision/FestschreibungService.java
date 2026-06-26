package com.bwa.controlling.revision;

import com.bwa.controlling.imports.Buchung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Festschreibung (Periodensiegelung) mit manipulationssicherer Hash-Kette und Verifikation.
 * Eine festgeschriebene Periode (Mandant+Monat) darf keine weiteren Buchungen mehr erhalten.
 */
@Service
public class FestschreibungService {

    public record PruefErgebnis(boolean unveraendert, List<String> abweichendeMonate) {}

    private final FestschreibungRepository festRepo;
    private final PeriodenBuchungRepository buchungen;
    private final SicherheitsKontext kontext;
    private final AuditService audit;

    public FestschreibungService(FestschreibungRepository festRepo, PeriodenBuchungRepository buchungen,
                                 SicherheitsKontext kontext, AuditService audit) {
        this.festRepo = festRepo;
        this.buchungen = buchungen;
        this.kontext = kontext;
        this.audit = audit;
    }

    public boolean istFestgeschrieben(String mandant, String monat) {
        return festRepo.existsByMandantAndMonat(mandant, monat);
    }

    public List<Festschreibung> fuerMandant(String mandant) {
        return festRepo.findByMandantOrderByMonatAsc(mandant);
    }

    @Transactional
    public Festschreibung festschreibe(String mandant, String monat) {
        if (festRepo.existsByMandantAndMonat(mandant, monat)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Periode bereits festgeschrieben: " + mandant + " " + monat);
        }
        int jahr = Integer.parseInt(monat.substring(0, 4));
        List<Buchung> periode = buchungen.findByMandantAndMonatOrderByIdAsc(mandant, monat);
        String vorgaenger = festRepo.findFirstByMandantOrderByMonatDesc(mandant)
                .map(Festschreibung::getPeriodenHash).orElse("");
        String hash = sha256(vorgaenger + kanonisch(periode));

        Festschreibung f = festRepo.save(new Festschreibung(
                mandant, jahr, monat, kontext.sub(), periode.size(), vorgaenger, hash));
        audit.protokolliere("FESTSCHREIBUNG", "festschreibung", mandant + " " + monat,
                "Buchungen=" + periode.size() + ", hash=" + hash.substring(0, 12) + "…");
        return f;
    }

    /** Prüft die Hash-Kette gegen die aktuellen Buchungen (erkennt nachträgliche Manipulation). */
    @Transactional(readOnly = true)
    public PruefErgebnis pruefe(String mandant) {
        List<String> abweichungen = new ArrayList<>();
        for (Festschreibung f : festRepo.findByMandantOrderByMonatAsc(mandant)) {
            List<Buchung> periode = buchungen.findByMandantAndMonatOrderByIdAsc(mandant, f.getMonat());
            String neu = sha256((f.getVorgaengerHash() == null ? "" : f.getVorgaengerHash()) + kanonisch(periode));
            if (!neu.equals(f.getPeriodenHash())) {
                abweichungen.add(f.getMonat());
            }
        }
        return new PruefErgebnis(abweichungen.isEmpty(), abweichungen);
    }

    private static String kanonisch(List<Buchung> periode) {
        StringBuilder sb = new StringBuilder();
        for (Buchung b : periode) {
            sb.append(b.getKonto()).append(';')
              .append(b.getBetrag().toPlainString()).append(';')
              .append(b.getSollHaben() == null ? "" : b.getSollHaben()).append(';')
              .append(b.getBwaGruppe() == null ? "" : b.getBwaGruppe()).append('\n');
        }
        return sb.toString();
    }

    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

interface FestschreibungRepository extends JpaRepository<Festschreibung, Long> {
    boolean existsByMandantAndMonat(String mandant, String monat);
    List<Festschreibung> findByMandantOrderByMonatAsc(String mandant);
    Optional<Festschreibung> findFirstByMandantOrderByMonatDesc(String mandant);
}

interface PeriodenBuchungRepository extends Repository<Buchung, Long> {
    @Query("select b from Buchung b where b.mandant = :mandant and b.monat = :monat order by b.id")
    List<Buchung> findByMandantAndMonatOrderByIdAsc(String mandant, String monat);
}

package com.bwa.controlling.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Schreibt Audit-Einträge für mutierende Operationen. Append-only. */
@Service
public class AuditService {

    private final AuditRepository repository;
    private final SicherheitsKontext kontext;

    public AuditService(AuditRepository repository, SicherheitsKontext kontext) {
        this.repository = repository;
        this.kontext = kontext;
    }

    public void protokolliere(String aktion, String entitaet, String ref, String details) {
        repository.save(new AuditEintrag(
                kontext.sub(), kontext.benutzername(), aktion, entitaet, ref, details));
    }

    public List<AuditEintrag> alle() {
        return repository.findAllByOrderByZeitpunktDesc();
    }
}

interface AuditRepository extends JpaRepository<AuditEintrag, Long> {
    List<AuditEintrag> findAllByOrderByZeitpunktDesc();
}

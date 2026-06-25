package com.bwa.controlling.benutzer;

import com.bwa.controlling.stammdaten.StammdatenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Setzt die Mandanten-Datentrennung durch: ermittelt aus dem aktuellen JWT (sub-Claim), welche
 * Mandanten der Benutzer sehen darf. Admins sehen alle; andere Benutzer nur ihre Zuweisungen.
 * Neue Benutzer werden beim ersten Zugriff angelegt (secure-by-default: zunächst kein Mandant).
 */
@Service
public class MandantenZugriffService {

    private final BenutzerRepository repository;
    private final StammdatenService stammdaten;

    public MandantenZugriffService(BenutzerRepository repository, StammdatenService stammdaten) {
        this.repository = repository;
        this.stammdaten = stammdaten;
    }

    @Transactional
    public Benutzer aktuellerBenutzer() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String sub = auth.getToken().getSubject();
        String benutzername = auth.getToken().getClaimAsString("preferred_username");
        return repository.findById(sub).orElseGet(() -> repository.save(new Benutzer(sub, benutzername)));
    }

    public boolean istAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }

    public boolean siehtAlle() {
        return istAdmin() || aktuellerBenutzer().isAlleMandanten();
    }

    @Transactional(readOnly = true)
    public Set<String> erlaubteMandanten() {
        if (siehtAlle()) {
            return new HashSet<>(stammdaten.alleMandantennamen());
        }
        return new HashSet<>(aktuellerBenutzer().getMandanten());
    }

    /** Wirft 403, wenn der aktuelle Benutzer den Mandanten nicht sehen darf. */
    public void pruefe(String mandant) {
        if (!erlaubteMandanten().contains(mandant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf Mandant: " + mandant);
        }
    }

    /** Reduziert eine Mandantenliste auf die erlaubten Einträge. */
    public List<String> filtere(List<String> mandanten) {
        Set<String> erlaubt = erlaubteMandanten();
        return mandanten.stream().filter(erlaubt::contains).toList();
    }
}

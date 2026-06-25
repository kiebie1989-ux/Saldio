package com.bwa.controlling.benutzer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

/**
 * Admin-Verwaltung der Mandanten-Berechtigung je Benutzer (nur Rolle admin).
 * Benutzer erscheinen hier, sobald sie sich erstmals angemeldet und einen Zugriff ausgelöst haben.
 */
@RestController
@RequestMapping("/api/benutzer")
public class BenutzerController {

    private final BenutzerRepository repository;

    public BenutzerController(BenutzerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<BenutzerDto> alle() {
        return repository.findAllByOrderByBenutzernameAsc().stream().map(BenutzerController::toDto).toList();
    }

    @PutMapping
    public BenutzerDto aktualisiere(@RequestBody BenutzerUpdate body) {
        Benutzer b = repository.findById(body.sub())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Benutzer nicht gefunden: " + body.sub()));
        b.setAlleMandanten(body.alleMandanten());
        b.setMandanten(body.mandanten() == null ? new HashSet<>() : new HashSet<>(body.mandanten()));
        return toDto(repository.save(b));
    }

    private static BenutzerDto toDto(Benutzer b) {
        return new BenutzerDto(b.getSub(), b.getBenutzername(), b.isAlleMandanten(),
                b.getMandanten().stream().sorted().toList());
    }

    public record BenutzerDto(String sub, String benutzername, boolean alleMandanten, List<String> mandanten) {}

    public record BenutzerUpdate(String sub, boolean alleMandanten, List<String> mandanten) {}
}

package com.bwa.controlling.revision;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Festschreibung von Perioden + Verifikation der Hash-Kette (Admin). */
@RestController
@RequestMapping("/api/festschreibung")
public class FestschreibungController {

    private final FestschreibungService service;

    public FestschreibungController(FestschreibungService service) {
        this.service = service;
    }

    @GetMapping
    public List<Festschreibung> liste(@RequestParam String mandant) {
        return service.fuerMandant(mandant);
    }

    @PostMapping
    public Festschreibung festschreiben(@RequestBody FestschreibungAnfrage anfrage) {
        return service.festschreibe(anfrage.mandant(), anfrage.monat());
    }

    @GetMapping("/pruefen")
    public FestschreibungService.PruefErgebnis pruefen(@RequestParam String mandant) {
        return service.pruefe(mandant);
    }

    public record FestschreibungAnfrage(String mandant, String monat) {}
}

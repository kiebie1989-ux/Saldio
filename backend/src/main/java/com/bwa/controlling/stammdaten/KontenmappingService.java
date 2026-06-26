package com.bwa.controlling.stammdaten;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Kontenmapping (VLOOKUP-Äquivalent des Excel-Blatts 01_Saldenlisten_Import):
 * löst eine Kontonummer (SKR03 bevorzugt, sonst SKR04) zu BWA-Gruppe und GuV/Bilanz-Position auf.
 *
 * Wie Excel-VLOOKUP wird bei mehrdeutigen Kontonummern der erste Treffer verwendet.
 */
@Service
public class KontenmappingService {

    private static final Logger log = LoggerFactory.getLogger(KontenmappingService.class);

    public record Mapping(String bwaGruppe, String guvBilanzPosition, String vorzeichen, String kontenklasse) {}

    private final Map<String, Mapping> bySkr03 = new HashMap<>();
    private final Map<String, Mapping> bySkr04 = new HashMap<>();
    private final TreeSet<String> doppelteKonten = new TreeSet<>();

    public KontenmappingService(KontenrahmenRepository repository) {
        List<Kontenrahmen> alle = repository.findAll();
        for (Kontenrahmen k : alle) {
            Mapping m = new Mapping(k.getBwaGruppe(), k.getGuvBilanzPosition(), k.getVorzeichen(), k.getKontenklasse());
            erfasse(bySkr03, k.getSkr03(), m, "SKR03");
            erfasse(bySkr04, k.getSkr04(), m, "SKR04");
        }
        if (!doppelteKonten.isEmpty()) {
            log.warn("Kontenrahmen enthält mehrdeutige Kontonummern (erster Treffer gewinnt, "
                    + "bitte bereinigen): {}", doppelteKonten);
        }
    }

    private void erfasse(Map<String, Mapping> ziel, String konto, Mapping m, String rahmen) {
        if (konto == null) {
            return;
        }
        Mapping vorhanden = ziel.putIfAbsent(konto, m);
        if (vorhanden != null && !vorhanden.equals(m)) {
            doppelteKonten.add(rahmen + " " + konto);
        }
    }

    /** Mehrdeutige Kontonummern im Kontenrahmen (für Datenqualitätsprüfung). */
    public java.util.Set<String> doppelteKonten() {
        return java.util.Collections.unmodifiableSet(doppelteKonten);
    }

    /** Liefert das Mapping für eine Kontonummer, oder leer, wenn unbekannt. */
    public Optional<Mapping> resolve(String konto) {
        if (konto == null) {
            return Optional.empty();
        }
        String key = konto.trim();
        Mapping m = bySkr03.get(key);
        if (m == null) {
            m = bySkr04.get(key);
        }
        return Optional.ofNullable(m);
    }
}

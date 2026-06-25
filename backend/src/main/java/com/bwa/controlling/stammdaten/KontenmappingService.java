package com.bwa.controlling.stammdaten;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kontenmapping (VLOOKUP-Äquivalent des Excel-Blatts 01_Saldenlisten_Import):
 * löst eine Kontonummer (SKR03 bevorzugt, sonst SKR04) zu BWA-Gruppe und GuV/Bilanz-Position auf.
 *
 * Wie Excel-VLOOKUP wird bei mehrdeutigen Kontonummern der erste Treffer verwendet.
 */
@Service
public class KontenmappingService {

    public record Mapping(String bwaGruppe, String guvBilanzPosition, String vorzeichen) {}

    private final Map<String, Mapping> bySkr03 = new HashMap<>();
    private final Map<String, Mapping> bySkr04 = new HashMap<>();

    public KontenmappingService(KontenrahmenRepository repository) {
        List<Kontenrahmen> alle = repository.findAll();
        for (Kontenrahmen k : alle) {
            Mapping m = new Mapping(k.getBwaGruppe(), k.getGuvBilanzPosition(), k.getVorzeichen());
            if (k.getSkr03() != null) {
                bySkr03.putIfAbsent(k.getSkr03(), m);
            }
            if (k.getSkr04() != null) {
                bySkr04.putIfAbsent(k.getSkr04(), m);
            }
        }
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

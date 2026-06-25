package com.bwa.controlling.ki;

import com.bwa.controlling.engine.Kennzahl;
import com.bwa.controlling.ki.Mandantenbericht.Abschnitt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * LLM-gestützter Generator (Spring AI, Multi-Provider). Die strukturierten Bereichsanalysen
 * bleiben deterministisch (vom regelbasierten Generator); das LLM erzeugt nur den narrativen
 * Managementkommentar aus denselben Fakten. Bei jedem Fehler wird auf den regelbasierten Text
 * zurückgefallen — die KI kann also nie zu einem kaputten Bericht führen.
 */
@Component
public class LlmKommentarGenerator implements KommentarGenerator {

    private static final Logger log = LoggerFactory.getLogger(LlmKommentarGenerator.class);

    private final Map<String, ChatModel> chatModels;
    private final RegelbasierterKommentarGenerator regelbasiert;
    private final String provider;

    public LlmKommentarGenerator(Map<String, ChatModel> chatModels,
                                 RegelbasierterKommentarGenerator regelbasiert,
                                 @Value("${bwa.ki.provider:none}") String provider) {
        this.chatModels = chatModels;
        this.regelbasiert = regelbasiert;
        this.provider = provider;
    }

    @Override
    public String quelle() {
        return "KI: " + provider;
    }

    /** Ob für den konfigurierten Provider ein ChatModel verfügbar ist. */
    public boolean verfuegbar() {
        return modelFor(provider) != null;
    }

    @Override
    public Mandantenbericht erzeuge(BerichtsFakten fakten) {
        Mandantenbericht basis = regelbasiert.erzeuge(fakten);
        ChatModel model = modelFor(provider);
        if (model == null) {
            return basis;
        }
        try {
            String antwort = model.call(prompt(fakten));
            List<Abschnitt> kommentar = zuAbschnitten(antwort);
            if (kommentar.isEmpty()) {
                return basis;
            }
            return new Mandantenbericht(fakten.mandant(), fakten.jahr(), quelle(), basis.bereiche(), kommentar);
        } catch (Exception e) {
            log.warn("KI-Kommentar fehlgeschlagen ({}), nutze regelbasierten Fallback: {}", provider, e.toString());
            return new Mandantenbericht(fakten.mandant(), fakten.jahr(),
                    "regelbasiert (KI-Fallback)", basis.bereiche(), basis.managementkommentar());
        }
    }

    private ChatModel modelFor(String prov) {
        String beanName = switch (prov == null ? "none" : prov.toLowerCase()) {
            case "ollama" -> "ollamaChatModel";
            case "openai", "openrouter", "deepseek" -> "openAiChatModel";
            default -> null;
        };
        return beanName == null ? null : chatModels.get(beanName);
    }

    private static String prompt(BerichtsFakten f) {
        StringBuilder kz = new StringBuilder();
        for (Kennzahl k : f.kennzahlen()) {
            kz.append("- %s: %s %s (Ampel %s)%n".formatted(k.name(), k.wert(), k.einheit(), k.ampel()));
        }
        return """
                Du bist Controlling-Assistent eines Steuerberaters. Schreibe einen sachlichen
                Managementkommentar zur BWA des Mandanten "%s" für das Jahr %d auf Deutsch.
                Gliedere in genau sechs kurze Absätze in dieser Reihenfolge, je durch eine Leerzeile getrennt:
                Geschäftsentwicklung, Ertragslage, Kostenbewertung, Liquidität, Personal, Ausblick.
                Nutze AUSSCHLIESSLICH die folgenden Zahlen, erfinde nichts dazu.

                Umsatz YTD: %s, Rohertrag YTD: %s, EBIT YTD: %s, EBIT-Marge: %s %%, Mitarbeiter: %d.
                Kennzahlen:
                %s
                """.formatted(f.mandant(), f.jahr(),
                f.kpis().umsatzYtd(), f.kpis().rohertragYtd(), f.kpis().ebitYtd(),
                f.kpis().ebitMarge(), f.kpis().mitarbeiter(), kz.toString());
    }

    private static List<Abschnitt> zuAbschnitten(String antwort) {
        if (antwort == null || antwort.isBlank()) {
            return List.of();
        }
        String[] teile = antwort.strip().split("\\n\\s*\\n");
        return Arrays.stream(teile)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .map(s -> new Abschnitt("Managementkommentar", s))
                .toList();
    }
}

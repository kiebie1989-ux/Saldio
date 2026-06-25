package com.bwa.controlling.ki;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Testet den LLM-Generator mit gemocktem ChatModel — inkl. Fallback ohne echte Infrastruktur. */
class LlmKommentarGeneratorTest {

    private final RegelbasierterKommentarGenerator regelbasiert = new RegelbasierterKommentarGenerator();

    @Test
    void nutztLlmAntwortUndBehaeltDeterministischeBereiche() {
        ChatModel model = mock(ChatModel.class);
        when(model.call(anyString())).thenReturn(
                "Geschäftsentwicklung solide.\n\nErtragslage gut.\n\nKosten im Rahmen.\n\n"
                        + "Liquidität stark.\n\nPersonal effizient.\n\nAusblick positiv.");
        var gen = new LlmKommentarGenerator(Map.of("ollamaChatModel", model), regelbasiert, "ollama");

        assertThat(gen.verfuegbar()).isTrue();
        Mandantenbericht b = gen.erzeuge(KommentarFixtures.mustermann());

        assertThat(b.quelle()).isEqualTo("KI: ollama");
        assertThat(b.managementkommentar()).hasSize(6);
        assertThat(b.managementkommentar().get(0).text()).contains("Geschäftsentwicklung solide");
        // Bereiche bleiben deterministisch (aus dem regelbasierten Generator)
        assertThat(b.bereiche()).hasSize(6);
    }

    @Test
    void faelltBeiLlmFehlerAufRegelbasiertZurueck() {
        ChatModel model = mock(ChatModel.class);
        when(model.call(anyString())).thenThrow(new RuntimeException("LLM nicht erreichbar"));
        var gen = new LlmKommentarGenerator(Map.of("ollamaChatModel", model), regelbasiert, "ollama");

        Mandantenbericht b = gen.erzeuge(KommentarFixtures.mustermann());

        assertThat(b.quelle()).contains("Fallback");
        assertThat(b.managementkommentar()).hasSize(6); // regelbasierter Text
    }

    @Test
    void istNichtVerfuegbarOhnePassendesModell() {
        var gen = new LlmKommentarGenerator(Map.of(), regelbasiert, "ollama");
        assertThat(gen.verfuegbar()).isFalse();
    }
}

package com.bwa.controlling.ki;

import com.bwa.controlling.AbstractPostgresIT;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Beweist den OpenAI-kompatiblen Cloud-Provider-Pfad (OpenRouter/OpenAI/DeepSeek) OHNE echten
 * API-Key: ein MockWebServer simuliert die /v1/chat/completions-Antwort. Verifiziert, dass Spring AI
 * den konfigurierten Endpunkt aufruft und der LlmKommentarGenerator die Antwort verarbeitet.
 *
 * Der echte Cloud-Call ist mit einem API-Key ein Einzeiler (siehe README); er bleibt bewusst
 * außerhalb des Test-Suites (kein Secret im Build).
 */
@SpringBootTest
class LlmCloudProviderIT extends AbstractPostgresIT {

    static final MockWebServer MOCK = new MockWebServer();

    static {
        try {
            MOCK.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        String baseUrl = MOCK.url("/").toString().replaceAll("/$", "");
        registry.add("bwa.ki.provider", () -> "openrouter");
        registry.add("spring.ai.openai.base-url", () -> baseUrl);
        registry.add("spring.ai.openai.api-key", () -> "test-key");
    }

    @Autowired
    LlmKommentarGenerator generator;

    @Test
    void cloudProviderPfadRuftEndpunktUndVerarbeitetAntwort() throws InterruptedException {
        String antwort = """
                {"id":"x","object":"chat.completion","created":1,"model":"test",
                 "choices":[{"index":0,"message":{"role":"assistant",
                   "content":"Geschäftsentwicklung positiv.\\n\\nErtragslage stabil."},
                   "finish_reason":"stop"}],
                 "usage":{"prompt_tokens":1,"completion_tokens":1,"total_tokens":2}}
                """;
        MOCK.enqueue(new MockResponse().setBody(antwort).addHeader("Content-Type", "application/json"));

        Mandantenbericht bericht = generator.erzeuge(KommentarFixtures.mustermann());

        // Antwort des (gemockten) Cloud-Providers floss in den Bericht
        assertThat(bericht.quelle()).isEqualTo("KI: openrouter");
        assertThat(bericht.managementkommentar()).isNotEmpty();
        assertThat(bericht.managementkommentar().get(0).text()).contains("Geschäftsentwicklung positiv");

        // Spring AI hat den OpenAI-kompatiblen Endpunkt aufgerufen
        RecordedRequest request = MOCK.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).contains("/chat/completions");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-key");
    }
}

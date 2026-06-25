package com.bwa.poc.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * PoC-Gate: beweist die Multi-Provider-LLM-Abstraktion von Spring AI.
 *
 * Beide Provider werden als {@link ChatModel}-Beans auto-konfiguriert (Ollama + OpenAI-kompatibel).
 * Der aktive Provider wird ausschließlich über die Property {@code bwa.llm.provider} gewählt —
 * die Aufruflogik (ChatClient) bleibt identisch. Genau das ist die geforderte Austauschbarkeit:
 * Anthropic / OpenAI / DeepSeek / OpenRouter / Ollama unter einer API.
 */
@SpringBootApplication
public class LlmPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmPocApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    ApplicationRunner runner(Map<String, ChatModel> chatModels,
                             @Value("${bwa.llm.provider}") String provider) {
        return (ApplicationArguments appArgs) -> {
            String beanName = switch (provider.toLowerCase()) {
                case "ollama" -> "ollamaChatModel";
                case "openrouter", "openai", "deepseek" -> "openAiChatModel";
                default -> throw new IllegalArgumentException("Unbekannter Provider: " + provider);
            };
            ChatModel model = chatModels.get(beanName);
            if (model == null) {
                throw new IllegalStateException("Kein ChatModel-Bean '" + beanName
                        + "'. Verfügbar: " + chatModels.keySet());
            }

            String prompt = appArgs.getNonOptionArgs().isEmpty()
                    ? "Antworte in genau einem Satz auf Deutsch: Was ist eine BWA im Rechnungswesen?"
                    : String.join(" ", appArgs.getNonOptionArgs());

            System.out.println("=== LLM-PoC ===");
            System.out.println("Provider : " + provider + "  (Bean: " + beanName + ")");
            System.out.println("Verfügbar: " + chatModels.keySet());
            System.out.println("Prompt   : " + prompt);

            long t0 = System.currentTimeMillis();
            String answer = ChatClient.create(model)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            long ms = System.currentTimeMillis() - t0;

            System.out.println("Antwort  : " + (answer == null ? "<null>" : answer.strip()));
            System.out.println("Dauer    : " + ms + " ms");
            System.out.println("=== OK ===");
        };
    }
}

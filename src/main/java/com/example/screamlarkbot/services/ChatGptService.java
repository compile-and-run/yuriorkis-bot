package com.example.screamlarkbot.services;

import com.example.screamlarkbot.lang.Translator;
import com.example.screamlarkbot.models.gpt.ChatGptRequest;
import com.example.screamlarkbot.models.gpt.ChatGptResponse;
import com.example.screamlarkbot.models.gpt.Choice;
import com.example.screamlarkbot.models.gpt.Message;
import com.example.screamlarkbot.utils.Files;
import com.example.screamlarkbot.utils.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptService {
    private static final String URL = "https://api.openai.com/v1/chat/completions";
    private static String personality = Files.getResourceFileAsString("gpt-system.txt");

    private static final int MAX_SIZE = 500 - 27; // 27 is for a nickname

    private static final int SHORT_COOL_DOWN = 12; // in seconds
    private static final int RESPONSES_PER_HOUR = 35;

    private final RestTemplate restTemplate;

    private final Translator translator;

    @Value("${screamlark-bot.gpt-key}")
    private String gptKey;

    private Instant lastMessageTime = Instant.MIN;
    private int responses = RESPONSES_PER_HOUR;

    public static void setPersonality(String value) {
        personality = value;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public synchronized void updateResponseLimit() {
        responses = RESPONSES_PER_HOUR;
        log.info("response limit has been updated!");
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 10)
    public CompletableFuture<List<String>> generateWithCoolDown(List<Message> messages) {
        String username = messages.get(messages.size() - 1).getName();
        synchronized (this) {
            if (lastMessageTime.plus(SHORT_COOL_DOWN, ChronoUnit.SECONDS).isAfter(Instant.now())) {
                var response = translator.toLocale("busy");
                return CompletableFuture.completedFuture(List.of(Messages.reply(username, response)));
            }
            if (responses <= 0) {
                var response = translator.toLocale("resting");
                return CompletableFuture.completedFuture(List.of(Messages.reply(username, response)));
            }
            responses--;
            lastMessageTime = Instant.now();
        }
        return generate(messages);
    }

    @Async
    @Retryable(value = Exception.class, maxAttempts = 10)
    public CompletableFuture<List<String>> generate(List<Message> messages) {
        log.info("ChatGPT is working...");

        var system = personality;
        if (translator.getLocale().equals(Locale.ENGLISH)) {
            system += "Please, always answer in English.";
        } else {
            system += "Please, always answer in Russian.";
        }

        List<Message> requestMessages = new ArrayList<>();
        requestMessages.add(new Message("system", null, system));
        requestMessages.addAll(messages);

        var request = ChatGptRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(requestMessages)
            .temperature(0.7f)
            .maxTokens(500)
            .build();

        var headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + gptKey);

        try {
            ResponseEntity<ChatGptResponse> response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ChatGptResponse.class
            );

            if (!response.hasBody()) {
                log.info("response body is empty");
                return CompletableFuture.completedFuture(List.of());
            }

            ChatGptResponse body = response.getBody();
            if (body.getChoices() == null || body.getChoices().isEmpty()) {
                log.info("there is no choices");
                return CompletableFuture.completedFuture(List.of());
            }

            Choice choice = body.getChoices().get(0);
            var answer = choice.getMessage().getContent();
            List<String> responses = split(answer, MAX_SIZE);

            return CompletableFuture.completedFuture(responses);
        } catch (Exception e) {
            log.error("there's been an exception while sending a request to chatGPT", e);
            throw new RuntimeException(e);
        }
    }

    private static List<String> split(String s, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < s.length(); i += chunkSize) {
            chunks.add(s.substring(i, Math.min(s.length(), i + chunkSize)));
        }
        return chunks;
    }
}

package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.pbot.PBotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class PBotService {

    private static final String PBOT_URL = "http://p-bot.ru/api/getAnswer";

    private final RestTemplate restTemplate;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @Async
    public CompletableFuture<String> getAnswer(String username, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("request", message);
        map.add("bot_name", botName);
        map.add("user_name", username);
        map.add("dialog_lang", "ru");
        map.add("dialog_id", UUID.randomUUID().toString());

        // IDK what it is
        map.add("a", "public-api");
        map.add("b", "772247888");
        map.add("c", "1061668088");
        map.add("d", "1523365651");
        map.add("e", "0.004262932444960343");
        map.add("t", "1662146744665");
        map.add("x", "1.1644375462831635");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<PBotResponse> response = restTemplate.exchange(PBOT_URL, HttpMethod.POST, entity, PBotResponse.class);

        if (response.hasBody()) {
            return CompletableFuture.completedFuture(response.getBody().getAnswer());
        }
        return CompletableFuture.completedFuture(null);
    }
}

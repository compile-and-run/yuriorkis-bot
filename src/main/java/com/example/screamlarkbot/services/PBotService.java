package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.pbot.Dialog;
import com.example.screamlarkbot.models.pbot.PBotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

@Slf4j
@RequiredArgsConstructor
@Service
public class PBotService {

    private static final String PBOT_URL = "http://p-bot.ru/api/getAnswer";

    private final RestTemplate restTemplate;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    // username -> dialog
    private final Map<String, Dialog> dialogs = new ConcurrentHashMap<>();

    @Async
    @Retryable(value = Exception.class, maxAttempts = 10)
    public CompletableFuture<String> getAnswer(String username, String message) {
        log.info("pbot is working...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("request", message);
        map.add("bot_name", botName);
        map.add("user_name", username);
        map.add("dialog_lang", "ru");

        Dialog dialog = dialogs.computeIfAbsent(username, key -> new Dialog());
        map.add("dialog_id", dialog.getDialogId());

        var history = dialog.getHistory();
        map.add("answer_1", history.get(5));
        map.add("request_1", history.get(4));
        map.add("answer_2", history.get(3));
        map.add("request_2", history.get(2));
        map.add("answer_3", history.get(1));
        map.add("request_3", history.get(0));

        generateUrlSign(map);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        log.info("send response to pbot api " + map);
        ResponseEntity<PBotResponse> response = restTemplate.exchange(PBOT_URL, HttpMethod.POST, entity, PBotResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("status code is " + response.getStatusCode());
            throw new RuntimeException("status code is not 200");
        }

        if (response.hasBody()) {
            log.info("response from pbot: " + response);
            var answer = response.getBody().getAnswer();
            dialog.addRequestAndAnswer(message, answer);
            return CompletableFuture.completedFuture(answer);
        }
        log.info("response has no body");
        return CompletableFuture.completedFuture(null);
    }

    private void generateUrlSign(MultiValueMap<String, String> map) {
        var time = Long.toString(Instant.now().toEpochMilli());
        map.add("a", "public-api");
        map.add("b", computeCRC32(time + "b"));
        map.add("c", generateCRCSign(time));
        map.add("d", computeCRC32(Instant.now().toEpochMilli() + "d"));
        map.add("e", Double.toString(Math.random()));
        map.add("t", time);
        map.add("x", Double.toString(Math.random() * 10));
    }

    private String computeCRC32(String value) {
        var crc = new CRC32();
        crc.update(value.getBytes());
        return Long.toString(crc.getValue());
    }

    private String generateCRCSign(String value) {
        var k1 = "qVxRWnespIsJg7DxFbF6N9FiQR5cjnHy";
        var k21 = "ygru3JcToH4dPdiN";
        return computeCRC32("public-api" + value + k1 + k21 + "H5SXOYIc00qMXPKJ");
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public synchronized void removeOldDialogs() {
        dialogs.entrySet()
                .removeIf(entry -> Duration.between(entry.getValue().getLastUpdate(), LocalDateTime.now()).toMinutes() >= 3);
    }
}

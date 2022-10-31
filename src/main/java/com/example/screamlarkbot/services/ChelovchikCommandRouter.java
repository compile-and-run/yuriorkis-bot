package com.example.screamlarkbot.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChelovchikCommandRouter {
    private final RestTemplate restTemplate;

    @Value("${screamlark-bot.chelovchik-bot-url}")
    private String url;

    @Value("${screamlark-bot.chelovchik-bot-token}")
    private String token;

    public Optional<String> route(String username, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + token);
            var response = restTemplate.exchange(
                url + "/api/runcommand",
                HttpMethod.POST,
                new HttpEntity<>(new Request(username, text), headers),
                Response.class
            );
            if (response.hasBody()) {
                return Optional.ofNullable(response.getBody().text);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error occurred while sending a request to ChelovchikBot", e);
            return Optional.empty();
        }
    }

    @Data
    @AllArgsConstructor
    private static class Request {
        private String username;
        private String text;
    }

    @Data
    private static class Response {
        private String text;
    }
}

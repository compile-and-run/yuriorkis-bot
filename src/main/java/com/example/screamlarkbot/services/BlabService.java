package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.blab.BlabRequest;
import com.example.screamlarkbot.models.blab.BlabResponse;
import com.example.screamlarkbot.models.blab.BlabStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class BlabService {

    private static final String URL = "https://zeapi.yandex.net/lab/api/yalm/text3";
    private final RestTemplate restTemplate;

    @Async
    public CompletableFuture<String> generate(BlabStyle style, String query) {
        if (query.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        BlabRequest request = BlabRequest.builder()
                .intro(style.getIntro())
                .query(query)
                .build();
        ResponseEntity<BlabResponse> response = restTemplate.postForEntity(URL, request, BlabResponse.class);
        if (response.hasBody()) {
            BlabResponse body = response.getBody();
            return CompletableFuture.completedFuture(body.getQuery() + body.getText());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

}

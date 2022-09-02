package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.blab.BlabRequest;
import com.example.screamlarkbot.models.blab.BlabResponse;
import com.example.screamlarkbot.models.blab.BlabStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlabService {

    private static final String URL = "https://zeapi.yandex.net/lab/api/yalm/text3";
    private final RestTemplate restTemplate;

    public Optional<String> generate(BlabStyle style, String query) {
        if (query.isBlank()) {
            return Optional.empty();
        }
        BlabRequest request = BlabRequest.builder()
                .intro(style.getIntro())
                .query(query)
                .build();
        ResponseEntity<BlabResponse> response = restTemplate.postForEntity(URL, request, BlabResponse.class);
        if (response.hasBody()) {
            return Optional.of(response.getBody().getText());
        } else {
            return Optional.empty();
        }
    }

}

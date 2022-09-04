package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.stablediffusion.ReplicateRequest;
import com.example.screamlarkbot.models.stablediffusion.ReplicateRequest.ReplicateInput;
import com.example.screamlarkbot.models.stablediffusion.ReplicateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class StableDiffusionService {

    private static final String REPLICATE_URL = "https://api.replicate.com/v1/predictions";
    private static final String VERSION = "a9758cbfbd5f3c2094457d996681af52552901775aa2d6dd0b17fd15df959bef";

    @Value("${screamlark-bot.replicate-token}")
    private String replicateToken;

    private final RestTemplate restTemplate;

    @Async
    public CompletableFuture<String> generateImage(String text) {
        log.info("stable diffusion is working...");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + replicateToken);

        ReplicateRequest request = new ReplicateRequest(VERSION, ReplicateInput.builder().prompt(text).build());
        HttpEntity<ReplicateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ReplicateResponse>  response = restTemplate.exchange(REPLICATE_URL, HttpMethod.POST, entity, ReplicateResponse.class);

        if (!response.hasBody()) {
            log.error("response has no body");
            return CompletableFuture.completedFuture(null);
        }

        while (response.getBody().getOutput() == null) {
            log.info("trying to get the result...");
            String getUrl = response.getBody().getUrls().getGet();
            response = restTemplate.exchange(getUrl, HttpMethod.GET, entity, ReplicateResponse.class);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.info("stability ai was interrupted", e);
                Thread.currentThread().interrupt();
                return CompletableFuture.completedFuture(null);
            }
        }
        return CompletableFuture.completedFuture(response.getBody().getOutput()[0]);
    }
}

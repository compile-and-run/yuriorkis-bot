package com.example.screamlarkbot.models.gpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ChatGptRequest {
    private final String model;
    private final List<Message> messages;
    private final float temperature;
    @JsonProperty("max_tokens")
    private final int maxTokens;
}

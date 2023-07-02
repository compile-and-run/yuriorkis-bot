package com.example.screamlarkbot.models.gpt;

import lombok.Data;

import java.util.List;

@Data
public class ChatGptResponse {
    private List<Choice> choices;
}

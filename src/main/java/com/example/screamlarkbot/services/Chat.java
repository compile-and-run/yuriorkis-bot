package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.gpt.Message;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final int MAX_SIZE = 15;
    private final List<Message> messages = new ArrayList<>();

    public synchronized void addUserMessage(String name, String message) {
        messages.add(new Message("user", name, message));
        if (messages.size() > MAX_SIZE) {
            messages.remove(0);
        }
    }

    public synchronized void addBotMessage(String message) {
        messages.add(new Message("assistant", null, message));
        if (messages.size() > MAX_SIZE) {
            messages.remove(0);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }
}

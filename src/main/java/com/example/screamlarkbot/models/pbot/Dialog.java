package com.example.screamlarkbot.models.pbot;

import liquibase.repackaged.org.apache.commons.collections4.queue.CircularFifoQueue;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Dialog {
    @Getter
    private final String dialogId = UUID.randomUUID().toString();
    @Getter
    private LocalDateTime lastUpdate = LocalDateTime.now();
    private final Queue<String> history = new CircularFifoQueue<>(6);

    public Dialog() {
        for (int i = 0; i < 6; i++) {
            history.add("");
        }
    }

    public synchronized void addRequestAndAnswer(String request, String answer) {
        history.add(request);
        history.add(answer);
        lastUpdate = LocalDateTime.now();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }
}

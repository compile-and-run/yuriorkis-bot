package com.example.screamlarkbot.utils;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commands {
    public static void registerCommand(EventManager eventManager, String command, CommandHandler commandHandler) {
        eventManager.onEvent(ChannelMessageEvent.class, messageEvent -> {
            String message = messageEvent.getMessage();
            if (message.startsWith(command + " ") || message.equals(command)) {
                String args = message.substring(command.length());
                commandHandler.handleCommand(messageEvent, args);
            }
        });
    }

    public interface CommandHandler {
        void handleCommand(ChannelMessageEvent messageEvent, String args);
    }
}

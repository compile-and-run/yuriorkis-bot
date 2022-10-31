package com.example.screamlarkbot.utils;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commands {

    public static void registerCommand(EventManager eventManager, List<String> commands, CommandHandler commandHandler) {
        eventManager.onEvent(ChannelMessageEvent.class, messageEvent -> {
            String message = messageEvent.getMessage();
            for (String command : commands) {
                if (message.startsWith(command + " ") || message.equals(command)) {
                    String args = message.substring(command.length());
                    commandHandler.handleCommand(messageEvent, args);
                    return;
                }
            }
        });
    }

    public static void registerCommand(EventManager eventManager, String command, CommandHandler commandHandler) {
        registerCommand(eventManager, List.of(command), commandHandler);
    }

    public interface CommandHandler {
        void handleCommand(ChannelMessageEvent messageEvent, String args);
    }
}

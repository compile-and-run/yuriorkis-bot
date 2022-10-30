package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.ChelovchikCommandRouter;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChelovchikCommandHandler {

    private final TwitchClient twitchClient;

    private final ChelovchikCommandRouter commandRouter;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::commandHandler);
    }

    private void commandHandler(ChannelMessageEvent event) {
        String channel = event.getChannel().getName();
        String username = event.getUser().getName();
        String message = event.getMessage();
        if (message.startsWith("!")) {
            commandRouter.route(username, message.substring(1)).ifPresent(response ->
                twitchClient.getChat().sendMessage(channel, Messages.reply(username, response)));
        }
    }
}

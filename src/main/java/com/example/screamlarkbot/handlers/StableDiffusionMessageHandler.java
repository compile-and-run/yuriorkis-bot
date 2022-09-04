package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.StableDiffusionService;
import com.example.screamlarkbot.utils.Commands;
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
public class StableDiffusionMessageHandler {

    private final TwitchClient twitchClient;

    private final StableDiffusionService stableDiffusionService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        Commands.registerCommand(eventManager, "!generate", this::generateImage);
    }

    private void generateImage(ChannelMessageEvent event, String args) {
        var channel = event.getChannel().getName();
        var username = event.getUser().getName();
        stableDiffusionService.generateImage(args).thenAccept(img ->
                twitchClient.getChat().sendMessage(channel, Messages.reply(username, img)));
    }
}

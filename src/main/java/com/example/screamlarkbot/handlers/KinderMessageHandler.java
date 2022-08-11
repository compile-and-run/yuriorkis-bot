package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.services.KinderService;
import com.example.screamlarkbot.utils.Emotes;
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
public class KinderMessageHandler {

    private static final String KINDER_COMMAND = "!kinder";

    public final TwitchClient twitchClient;

    public final KinderService kinderService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::processKinderCommand);
    }

    private void processKinderCommand(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();

        if (message.startsWith(KINDER_COMMAND)) {
            String toyName = message.substring(KINDER_COMMAND.length()).trim();
            if (!toyName.isBlank()) {
                // add toy
                String response = "Игрушка %s была отправлена на фабрику киндеров! :)";
                response = addUsernameToMessage(username, response);
                twitchClient.getChat().sendMessage(event.getChannel().getName(), String.format(response, toyName));
                Toy toy = new Toy(null, toyName, event.getUser().getName());
                kinderService.addToy(toy);
            } else {
                // get toy
                Toy toy = kinderService.getRandomToy();
                if (toy == null) {
                    String response = "Киндеров еще нет " + Emotes.FEELS_WEAK_MAN.getName();
                    response = addUsernameToMessage(username, response);
                    twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
                    return;
                }
                String response = username + " открыл киндер и получил OOOO " + toyName + " peepoClap";
                twitchClient.getChat().sendMessage(event.getChannel().getName(), String.format(response, toy.getName()));
            }
        }

    }

    private String addUsernameToMessage(String username, String message) {
        return "@" + username + " " + message;
    }
}

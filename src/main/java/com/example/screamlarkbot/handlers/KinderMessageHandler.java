package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.exception.CoolDownException;
import com.example.screamlarkbot.exception.ToyExistsException;
import com.example.screamlarkbot.models.kinder.Toy;
import com.example.screamlarkbot.services.KinderService;
import com.example.screamlarkbot.utils.Emote;
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
public class KinderMessageHandler {

    private static final String KINDER_COMMAND = "!kinder";

    private final TwitchClient twitchClient;

    private final KinderService kinderService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::processKinderCommand);
    }

    private void processKinderCommand(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();

        if (message.equals(KINDER_COMMAND)) {
            // get toy
            String response;
            try {
                Toy toy = kinderService.getRandomToy(username);
                if (toy != null) {
                    response = username + " хочет обратиться к создателям киндер сюрприза Basedge вот такая хуйня попалась в яйце \uD83D\uDC49 %s ." +
                            " вы мне скажите, хоть один ребенок обрадуется Stare вот такой вот хуете в киндер сюрпризе? Madge";
                    twitchClient.getChat().sendMessage(event.getChannel().getName(), String.format(response, toy.getName()));
                    return;
                } else {
                    response = "Киндеров еще нет " + Emote.FEELS_WEAK_MAN;
                }
            } catch (CoolDownException e) {
                response = String.format("Ты недавно уже брал киндер %s Жди %s мин.", Emote.STARE, e.getMinutesLeft());
            }
            response = Messages.reply(username, response);
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
        if (message.startsWith(KINDER_COMMAND + " ")) {
            String toyName = message.substring(KINDER_COMMAND.length()).trim();
            if (!toyName.isBlank()) {
                // add toy
                Toy toy = Toy.builder()
                        .name(toyName)
                        .owner(event.getUser().getName())
                        .build();
                String response;
                try {
                    kinderService.addToy(toy);
                    response = String.format("Игрушка %s была отправлена на фабрику киндеров! :)", toyName);

                } catch (CoolDownException e) {
                    response = String.format("Ты недавно уже добавлял игрушку %s Жди %s мин.", Emote.STARE, e.getMinutesLeft());
                } catch (ToyExistsException e) {
                    response = "Прости, такая игрушка уже есть " + Emote.FEELS_WEAK_MAN;
                }
                response = Messages.reply(username, response);
                twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
            }
        }

    }
}

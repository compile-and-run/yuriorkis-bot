package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.utils.Emotes;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyMessageHandler {

    public final TwitchClient twitchClient;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        eventManager.onEvent(ChannelMessageEvent.class, this::sayHello);
        eventManager.onEvent(ChannelMessageEvent.class, this::reactOnLizardPls);
    }

    private void printChannelMessage(ChannelMessageEvent event) {
        log.info("[" + event.getChannel().getName() + "]["+event.getPermissions().toString()+"] " + event.getUser().getName() + ": " + event.getMessage());
    }

    private void sayHello(ChannelMessageEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().contains("@" + botName)) {
            String response = "@" + event.getUser().getName() + " " + Emotes.FROG_WAVE.getName();
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
    }

    private void reactOnLizardPls(ChannelMessageEvent event) {
        String message = event.getMessage();

        String[] words = message.split(" ");

        long lizardNumber = Arrays.stream(words).filter(w -> w.equals(Emotes.LIZARD_PLS.getName()))
                .count();

        if (lizardNumber > 0) {
            String response = IntStream.range(0, (int) lizardNumber)
                    .mapToObj(n -> Emotes.LIZARD_PLS.getName())
                    .collect(Collectors.joining(" "));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
    }
}

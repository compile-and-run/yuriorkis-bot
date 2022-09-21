package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.models.weak.WeakViewer;
import com.example.screamlarkbot.services.WeakViewerService;
import com.example.screamlarkbot.utils.Commands;
import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StdMessageHandler {

    private static final String WEAK_COMMAND = "!неосиляторы";

    private static final String WEAK_CHECK_COMMAND = "!неосилятор";

    private final TwitchClient twitchClient;

    private final WeakViewerService weakViewerService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::handleStd);
        Commands.registerCommand(eventManager, WEAK_COMMAND, this::handleWeakCommand);
        Commands.registerCommand(eventManager, WEAK_CHECK_COMMAND, this::handleWeakCheckCommand);
    }

    private void handleStd(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage().toLowerCase().replaceAll("\\s+"," ");
        if (message.matches(".*(?<!(scream|скрим) ?)[scс][tт][dд].*")) {
            weakViewerService.incrementScore(username);
            twitchClient.getChat().sendMessage(event.getChannel().getName(),
                    Messages.reply(username, "std для неосиляторов " + Emote.MADGE_KNIFE));
        }
    }

    private void handleWeakCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        List<WeakViewer> weakList = weakViewerService.getTop();

        if (weakList.isEmpty()) {
            twitchClient.getChat().sendMessage(channel, Messages.reply(username, "На канале нет неосиляторов " + Emote.PEEPO_CLAP));
            return;
        }

        StringBuilder result = new StringBuilder("Топ-5 неосиляторов канала: ");
        for (int i = 0; i < weakList.size(); i++) {
            WeakViewer viewer = weakList.get(i);
            result.append(i + 1).append(". ").append(viewer.getName()).append("(").append(viewer.getScore()).append(") ");
        }
        twitchClient.getChat().sendMessage(channel, Messages.reply(username, result.toString()));
    }

    private void handleWeakCheckCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();

        if (args.startsWith("@")) {
            args = args.substring(1);
        }

        if (args.isBlank()) {
            args = username;
        }

        String finalArgs = args;
        weakViewerService.findByName(args.trim())
                .ifPresentOrElse(user -> {
                    String message = finalArgs + "(" + user.getScore() + ") - неосилятор " + Emote.NOOOO;
                    twitchClient.getChat().sendMessage(channel, Messages.reply(username, message));
                }, () ->{
                    String message = finalArgs + " - осилятор " + Emote.PEEPO_SMART;
                    twitchClient.getChat().sendMessage(channel, Messages.reply(username, message));
                });
    }
}

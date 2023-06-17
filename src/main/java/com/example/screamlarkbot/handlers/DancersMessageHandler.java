package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.lang.Translator;
import com.example.screamlarkbot.models.dancer.DanceEmotes;
import com.example.screamlarkbot.models.dancer.DancerViewer;
import com.example.screamlarkbot.services.DancerViewerService;
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
public class DancersMessageHandler {

    private static final String DANCERS_COMMAND = "!dancers";

    private final TwitchClient twitchClient;

    private final DancerViewerService dancerViewerService;

    private final Translator translator;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::handleDance);
        Commands.registerCommand(eventManager, DANCERS_COMMAND, this::handleDancersCommand);
    }

    private void handleDance(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();
        List<String> danceEmotes = DanceEmotes.asStringList();

        for (String danceEmote : danceEmotes)
            if (message.matches("(.*)" + danceEmote + "(.*)"))
                dancerViewerService.incrementScore(username);
    }

    private void handleDancersCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        List<DancerViewer> dancersList = dancerViewerService.getTop();

        if (dancersList.isEmpty()) {
            String response = translator.toLocale("dancersEmpty");
            twitchClient.getChat().sendMessage(channel, Messages.reply(username, response));
            return;
        }

        String title = translator.toLocale("dancersTop");
        StringBuilder result = new StringBuilder(title + " ");
        for (int i = 0; i < dancersList.size(); i++) {
            DancerViewer viewer = dancersList.get(i);
            result.append(i + 1).append(". ").append(viewer.getName()).append("(").append(viewer.getScore()).append(") ");
        }
        twitchClient.getChat().sendMessage(channel, Messages.reply(username, result.toString()));
    }
}

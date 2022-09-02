package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.models.blab.BlabStyle;
import com.example.screamlarkbot.services.BlabService;
import com.example.screamlarkbot.utils.Commands;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlabMessageHandler {

    private static final String BLAB_COMMAND = "!блаб";
    private static final String INSTRUCTION_COMMAND = "!инструкция";
    private static final String RECIPE_COMMAND = "!рецепт";
    private static final String WISDOM_COMMAND = "!мудрость";
    private static final String STORY_COMMAND = "!история";
    private static final String WIKI_COMMAND = "!вики";
    private static final String SYNOPSIS_COMMAND = "!синопсис";

    private final TwitchClient twitchClient;

    private final BlabService blabService;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        Commands.registerCommand(eventManager, BLAB_COMMAND, this::handleBlabCommand);
        Commands.registerCommand(eventManager, INSTRUCTION_COMMAND, this::handleInstructionCommand);
        Commands.registerCommand(eventManager, RECIPE_COMMAND, this::handleRecipeCommand);
        Commands.registerCommand(eventManager, WISDOM_COMMAND, this::handleWisdomCommand);
        Commands.registerCommand(eventManager, STORY_COMMAND, this::handleStoryCommand);
        Commands.registerCommand(eventManager, WIKI_COMMAND, this::handleWikiCommand);
        Commands.registerCommand(eventManager, SYNOPSIS_COMMAND, this::handleSynopsisCommand);

        eventManager.onEvent(ChannelMessageEvent.class, this::sendRandomMessage);
    }

    private void handleBlabCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.NO_STYLE, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleInstructionCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.INSTRUCTION, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleRecipeCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.RECIPE, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleWisdomCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.WISDOM, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleStoryCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.STORY, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleWikiCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.WIKI, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void handleSynopsisCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.SYNOPSIS, args)
                .whenComplete((text, t) -> sendReply(channel, username, text));
    }

    private void sendReply(String channel, String username, String text) {
        if (text == null ) return;
        twitchClient.getChat().sendMessage(channel, Messages.reply(username, text));
    }

    private void sendRandomMessage(ChannelMessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("@" + botName)) return;
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 30);
        if (randomNumber == 0) {
            String text = message.replace("[^А-Яа-я]+ ", "").trim();
            if (text.isEmpty()) return;
            String[] words = text.split(" ");
            int randomIndex = ThreadLocalRandom.current().nextInt(0, words.length);
            handleWisdomCommand(event, words[randomIndex]);
        }
    }
}

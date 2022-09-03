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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
    }

    private void handleBlabCommand(ChannelMessageEvent event, String args) {
        log.info("handling blab command...");
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.NO_STYLE, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleInstructionCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.INSTRUCTION, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleRecipeCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.RECIPE, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleWisdomCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.WISDOM, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleStoryCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.STORY, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleWikiCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.WIKI, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void handleSynopsisCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();
        blabService.generate(BlabStyle.SYNOPSIS, args)
                .thenAccept(text -> sendReply(channel, username, text));
    }

    private void sendReply(String channel, String username, String text) {
        twitchClient.getChat().sendMessage(channel, Messages.reply(username, text));
    }
}

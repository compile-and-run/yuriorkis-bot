package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.PascalService;
import com.example.screamlarkbot.utils.Commands;
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
public class PascalMessageHandler {

    private static final String PASCAL_CHECK_COMMAND = "!паскаленок";
    private final TwitchClient twitchClient;

    private final PascalService pascalService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        Commands.registerCommand(eventManager, PASCAL_CHECK_COMMAND, this::handlePascalCheckCommand);
    }

    private void handlePascalCheckCommand(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String channel = event.getChannel().getName();

        if (args.startsWith("@")) {
            args = args.substring(1);
        }

        if (args.isBlank()) {
            args = username;
        }

        String finalArgs = args.toLowerCase().trim();
        if (pascalService.checkByUsername(finalArgs)) {
            String response = "%s Паскаленок обнаружен! %s Пользователь %s фолловер канала Turborium!";
            twitchClient.getChat().sendMessage(channel, Messages.reply(username, String.format(response, Emote.OOOO, Emote.OOOO, args)));
        } else {
            String response = "%s не паскаленок %s";
            twitchClient.getChat().sendMessage(channel, Messages.reply(username, String.format(response, args, Emote.PEEPO_SMART)));
        }
    }
}

package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.lang.Translator;
import com.example.screamlarkbot.utils.Commands;
import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.example.screamlarkbot.utils.TwitchHelper;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonEventHandler {

    private static final String HELP_COMMAND = "!help";
    private static final String LANG_COMMAND = "!lang";
    private static final int MIN_DAYS_AFTER_CREATION = 7;

    private final TwitchClient twitchClient;

    private final Translator translator;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        Commands.registerCommand(eventManager, HELP_COMMAND, this::processHelp);
        Commands.registerCommand(eventManager, LANG_COMMAND, this::processLang);
        eventManager.onEvent(ChannelMessageEvent.class, this::reactOnLizardPls);
        eventManager.onEvent(FollowEvent.class, this::handleFollow);
        eventManager.onEvent(SubscriptionEvent.class, this::handleSubscription);
        eventManager.onEvent(ChannelGoLiveEvent.class, this::handleGoLive);
        eventManager.onEvent(ChannelGoOfflineEvent.class, this::handleGoOffline);
        eventManager.onEvent(UserBanEvent.class, this::handleBan);
        eventManager.onEvent(ChannelJoinEvent.class, this::detectTurborium);
    }

    private void printChannelMessage(ChannelMessageEvent event) {
        String channelName = event.getChannel().getName();
        String permissions = event.getPermissions().toString();
        String username = event.getUser().getName();
        String message = event.getMessage();
        log.info("[" + channelName + "][" + permissions + "] " + username + ": " + message);
    }

    private void processHelp(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String response = translator.toLocale("help");
        response = Messages.reply(username, response);
        twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
    }

    private void processLang(ChannelMessageEvent event, String args) {
        String channel = event.getChannel().getName();
        String username = event.getUser().getName();
        boolean isMod = event.getPermissions().contains(CommandPermission.MODERATOR);
        if (isMod) {
            args = args.trim();
            if ("en".equals(args)) {
                translator.setLocale(Locale.forLanguageTag("en"));
                twitchClient.getChat().sendMessage(channel, Messages.reply(username, "Bloody hell, mate! I speak English! VeryBased"));
            } else if ("ru".equals(args)) {
                translator.setLocale(Locale.forLanguageTag("ru"));
                twitchClient.getChat().sendMessage(channel, Messages.reply(username, "Теперь я говорю по-русски VeryPog"));
            } else {
                String response = translator.toLocale("unknownLang");
                twitchClient.getChat().sendMessage(channel, Messages.reply(username, response));
            }
        }
    }

    private void reactOnLizardPls(ChannelMessageEvent event) {
        String message = event.getMessage();

        String[] words = message.split(" ");

        long lizardNumber = Arrays.stream(words)
                .filter(w -> w.equals(Emote.LIZARD_PLS.toString()))
                .count();

        if (lizardNumber > 0) {
            String response = IntStream.range(0, (int) lizardNumber)
                    .mapToObj(n -> Emote.LIZARD_PLS.toString())
                    .collect(Collectors.joining(" "));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
    }

    private void handleFollow(FollowEvent event) {
        String username = event.getUser().getName();
        String id = event.getUser().getId();
        log.info("'{}' followed", username);

        LocalDateTime createdAt = TwitchHelper.getCreatedAt(twitchClient, id);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() > MIN_DAYS_AFTER_CREATION) {
            String response = translator.toLocale("follow");
            twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
        }
    }

    private void handleSubscription(SubscriptionEvent event) {
        String username = event.getUser().getName();
        log.info("'{}' subscribed", username);
        String response = translator.toLocale("sub");
        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
    }

    private void handleGoLive(ChannelGoLiveEvent event) {
        String channelName = event.getChannel().getName();
        log.info("'{}' is live", channelName);
        String response = translator.toLocale("helloStreamer");
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, response));
    }

    private void handleGoOffline(ChannelGoOfflineEvent event) {
        String channelName = event.getChannel().getName();
        log.info("'{}' is offline", channelName);
        String response = translator.toLocale("buyStreamer");
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, response));
    }

    private void handleBan(UserBanEvent event) {
        String channelName = event.getChannel().getName();
        String username = event.getUser().getName();
        log.info("'{}' was banned", username);
        String response = translator.toLocale("ban");
        twitchClient.getChat().sendMessage(channelName, String.format(response, username));
    }

    private void detectTurborium(ChannelJoinEvent event) {
        String channelName = event.getChannel().getName();
        String username = event.getUser().getName();
        log.info("'{}' joined the channel", username);
        if ("turborium".equals(username)) {
            twitchClient.getChat().sendMessage(channelName, Emote.OOOO + " Внимание! Турбориум зашел на стрим! " + Emote.OOOO);
        }
    }
}

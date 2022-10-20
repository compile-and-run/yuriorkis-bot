package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.example.screamlarkbot.utils.TwitchHelper;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyMessageHandler {

    private static final String HELP_COMMAND = "!help";
    private static final String HELP_URL = "https://github.com/compile-and-run/screamlark-bot/blob/main/README.md";
    private static final int MIN_DAYS_AFTER_CREATION = 7;

    private final TwitchClient twitchClient;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        eventManager.onEvent(ChannelMessageEvent.class, this::processHelp);
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

    private void processHelp(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();
        if (message.equals(HELP_COMMAND)) {
            String response = Messages.reply(username, "Список команд тут: " + HELP_URL);
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
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
            String response = "Спасибо за фоллоу, добро пожаловать! " + Emote.PEEPO_CLAP;
            twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
        }
    }

    private void handleSubscription(SubscriptionEvent event) {
        String username = event.getUser().getName();
        log.info("'{}' subscribed", username);
        String response = "Спасибо за подписку, ты лучший! " + Emote.OOOO;
        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
    }

    private void handleGoLive(ChannelGoLiveEvent event) {
        String channelName = event.getChannel().getName();
        log.info("'{}' is live", channelName);
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, "Привет, стримлер! " + Emote.FROG_WAVE));
    }

    private void handleGoOffline(ChannelGoOfflineEvent event) {
        String channelName = event.getChannel().getName();
        log.info("'{}' is offline", channelName);
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, "Пока, стримлер " + Emote.FEELS_WEAK_MAN));
    }

    private void handleBan(UserBanEvent event) {
        String channelName = event.getChannel().getName();
        String username = event.getUser().getName();
        log.info("'{}' was banned", username);
        String response = "%s получил справедливый бан за мнение " + Emote.VERY_POG;
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

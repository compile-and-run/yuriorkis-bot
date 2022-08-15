package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.utils.Emotes;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.chat.events.channel.SubscriptionEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyMessageHandler {

    private static final String HELP_COMMAND = "!help";
    private static final String HELP_URL = "https://github.com/compile-and-run/screamlark-bot/blob/main/README.md";
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    private static final int MIN_DAYS_AFTER_CREATION = 7;

    private final TwitchClient twitchClient;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        eventManager.onEvent(ChannelMessageEvent.class, this::processHelp);
        eventManager.onEvent(ChannelMessageEvent.class, this::sayHello);
        eventManager.onEvent(ChannelMessageEvent.class, this::reactOnLizardPls);
        eventManager.onEvent(ChannelMessageEvent.class, this::detectNewViewers);
        eventManager.onEvent(FollowEvent.class, this::handleFollow);
        eventManager.onEvent(SubscriptionEvent.class, this::handleSubscription);
        eventManager.onEvent(ChannelGoLiveEvent.class, this::handleGoLive);
        eventManager.onEvent(ChannelGoOfflineEvent.class, this::handleGoOffline);
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

    private void sayHello(ChannelMessageEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("@" + botName.toLowerCase())) {
            String response = "@" + event.getUser().getName() + " " + Emotes.FROG_WAVE;
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
    }

    private void reactOnLizardPls(ChannelMessageEvent event) {
        String message = event.getMessage();

        String[] words = message.split(" ");

        long lizardNumber = Arrays.stream(words)
                .filter(w -> w.equals(Emotes.LIZARD_PLS.toString()))
                .count();

        if (lizardNumber > 0) {
            String response = IntStream.range(0, (int) lizardNumber)
                    .mapToObj(n -> Emotes.LIZARD_PLS.toString())
                    .collect(Collectors.joining(" "));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        }
    }

    private void detectNewViewers(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        if (!event.isDesignatedFirstMessage()) {
            return;
        }

        LocalDateTime createdAt = getCreatedAt(username);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() < MIN_DAYS_AFTER_CREATION) {
            String response = "%s Детектор сани зафиксировал подозрительную активность! %s Аккаунт %s был создан %s!";

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

            response = String.format(response, Emotes.OOOO, Emotes.OOOO, username, createdAt.format(dateFormatter));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        } else {
            String response = "Привет, новый зритель! " + Emotes.FROG_WAVE;
            twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
        }
    }

    private void handleFollow(FollowEvent event) {
        String username = event.getUser().getName();

        LocalDateTime createdAt = getCreatedAt(username);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() > MIN_DAYS_AFTER_CREATION) {
            String response = "Спасибо за фоллоу, добро пожаловать! " + Emotes.PEEPO_CLAP;
            twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
        }
    }

    private LocalDateTime getCreatedAt(String username) {
        UserList userList = twitchClient.getHelix().getUsers(null, null, List.of(username)).execute();
        User user = userList.getUsers().get(0);
        return LocalDateTime.ofInstant(user.getCreatedAt(), ZONE_ID);
    }

    private void handleSubscription(SubscriptionEvent event) {
        String username = event.getUser().getName();
        String response = "Спасибо за подписку, ты лучший! " + Emotes.OOOO;
        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
    }

    private void handleGoLive(ChannelGoLiveEvent event) {
        String channelName = event.getChannel().getName();
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, "Привет, стримлер! " + Emotes.FROG_WAVE));
    }

    private void handleGoOffline(ChannelGoOfflineEvent event) {
        String channelName = event.getChannel().getName();
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, "Пока, стримлер " + Emotes.FEELS_WEAK_MAN));
    }
}

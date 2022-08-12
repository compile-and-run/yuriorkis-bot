package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.utils.Emotes;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
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

    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    public final TwitchClient twitchClient;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @Value("${screamlark-bot.oauth}")
    private String oauth;

    @PostConstruct
    public void init() {
        // TODO: add spring handler
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        eventManager.onEvent(ChannelMessageEvent.class, this::sayHello);
        eventManager.onEvent(ChannelMessageEvent.class, this::reactOnLizardPls);
        eventManager.onEvent(ChannelMessageEvent.class, this::detectNewViewers);
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

    private void detectNewViewers(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String token = oauth.substring("oauth:".length());
        UserList userList = twitchClient.getHelix().getUsers(token, null, List.of(username)).execute();

        if (!event.isDesignatedFirstMessage() || userList.getUsers().isEmpty()) {
            return;
        }
        User user = userList.getUsers().get(0);
        LocalDateTime createdAt = LocalDateTime.ofInstant(user.getCreatedAt(), ZONE_ID);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() < 7) {
            String response = "OOOO Детектор сани зафиксировал подозрительную активность! OOOO Аккаунт %s был создан %s!";

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

            response = String.format(response, username, createdAt.format(dateFormatter));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
        } else {
            String response = "Привет, новый зритель! " + Emotes.FROG_WAVE.getName();
            twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
        }
    }
}

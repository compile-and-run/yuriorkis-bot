package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.PascalService;
import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.example.screamlarkbot.utils.TwitchHelper;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewViewerMessageHandler {
    private static final int MIN_DAYS_AFTER_CREATION = 7;
    private static final String DATE_PATTERN = "dd.MM.yyyy";

    private final TwitchClient twitchClient;

    private final PascalService pascalService;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::detectNewViewers);
    }

    private void detectNewViewers(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String userId = event.getUser().getId();
        if (!event.isDesignatedFirstMessage()) {
            return;
        }
        log.info("'{}' sent their first message", username);

        if (pascalService.checkByUserId(userId)) {
            String response = "%s Детектор паскалят зафиксировал подозрительную активность! %s Пользователь %s фолловер канала Turborium!";
            twitchClient.getChat().sendMessage(event.getChannel().getName(), String.format(response, Emote.OOOO, Emote.OOOO, username));
            return;
        }

        LocalDateTime createdAt = TwitchHelper.getCreatedAt(twitchClient, userId);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() < MIN_DAYS_AFTER_CREATION) {
            String response = "%s Детектор сани зафиксировал подозрительную активность! %s Аккаунт %s был создан %s!";

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

            response = String.format(response, Emote.OOOO, Emote.OOOO, username, createdAt.format(dateFormatter));
            twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
            return;
        }

        String response = "Привет, новый зритель! " + Emote.FROG_WAVE;
        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
    }
}

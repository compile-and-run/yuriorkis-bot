package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.PBotService;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@ConditionalOnProperty(value = "screamlark-bot.pbot", havingValue = "true")
@RequiredArgsConstructor
public class PBotMessageHandler {

    private final TwitchClient twitchClient;

    private final PBotService pBotService;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::sendReply);
    }
    
    // Компайл, принимай, не позорься (я не знаю этого апи, за гов*нокод изви*ни)
    // ((используй нормальные типы вместо var, а то в браузере даже не напишут, что это))

    private void sendReply(ChannelMessageEvent event) {
        var username = event.getUser().getName();
        var message = event.getMessage();
        if (message.toLowerCase().startsWith("@" + botName)) {
            message = message.substring(botName.length() + 1).trim();
            pBotService.getAnswer(username, message).thenAccept(answer -> {
                if (answer != null) {
                    if (username == "CMRDTND") {
                        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, "fight2"));
                    } else twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, answer));
                }
            });
        }
    }
}

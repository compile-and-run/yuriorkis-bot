package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.services.Chat;
import com.example.screamlarkbot.services.ChatGptService;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.StreamList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(value = "screamlark-bot.gpt", havingValue = "true")
@RequiredArgsConstructor
public class ChatGptMessageHandler {

    private final TwitchClient twitchClient;

    private final ChatGptService chatGptService;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    @Value("${screamlark-bot.channel-name}")
    private String channelName;
    @Value("${screamlark-bot.access-token}")
    private String accessToken;

    private final Chat chat = new Chat();

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::sendReply);
    }

    private void sendReply(ChannelMessageEvent event) {
        var username = event.getUser().getName();
        var message = event.getMessage();
        chat.addUserMessage(username, message);
        if (message.toLowerCase().startsWith("@" + botName)) {
            //if (!isChannelLive()) return;
            chatGptService.generate(chat).thenAccept(responses -> {
                if (!responses.isEmpty()) {
                    for (String response : responses) {
                        chat.addBotMessage(response);
                        twitchClient.getChat().sendMessage(event.getChannel().getName(), Messages.reply(username, response));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }

    private boolean isChannelLive() {
        StreamList streamList = twitchClient.getHelix().getStreams(accessToken, null, null, null, null, null, null, List.of(channelName)).execute();
        return !streamList.getStreams().isEmpty();
    }
}

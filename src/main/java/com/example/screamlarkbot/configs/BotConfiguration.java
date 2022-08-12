package com.example.screamlarkbot.configs;

import com.example.screamlarkbot.utils.Emotes;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Slf4j
@Configuration
public class BotConfiguration {

    @Value("${screamlark-bot.channel-name}")
    private String channelName;
    @Value("${screamlark-bot.bot-name}")
    private String botName;
    @Value("${screamlark-bot.oauth}")
    private String oauth;

    @Bean
    public TwitchClient twitchClient() {
        OAuth2Credential credential = new OAuth2Credential(botName, oauth);
        var client = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .withEnableHelix(true)
                .build();
        client.getChat().joinChannel(channelName);
        client.getChat().sendMessage(channelName, Emotes.FROG_WAVE.getName());
        enableFollowEventListener(client);
        enableStreamEventListener(client);
        return client;
    }

    private void enableFollowEventListener(TwitchClient client) {
        String token = oauth.substring("oauth:".length());
        UserList users = client.getHelix().getUsers(token, null, Collections.singletonList(channelName)).execute();

        if (users.getUsers().size() == 1) {
            User user = users.getUsers().get(0);
            client.getClientHelper().enableFollowEventListener(user.getId(), user.getLogin());
        } else {
            log.error("Failed to add channel " + channelName + " to Follow Listener, maybe it doesn't exist!");
        }
    }

    private void enableStreamEventListener(TwitchClient client) {
        String token = oauth.substring("oauth:".length());
        UserList users = client.getHelix().getUsers(token, null, Collections.singletonList(channelName)).execute();

        if (users.getUsers().size() == 1) {
            User user = users.getUsers().get(0);
            client.getClientHelper().enableStreamEventListener(user.getId(), user.getLogin());
        } else {
            log.error("Failed to add channel {} to stream event listener!", channelName);
        }
    }
}

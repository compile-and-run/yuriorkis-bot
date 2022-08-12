package com.example.screamlarkbot.configs;

import com.example.screamlarkbot.utils.Emotes;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return client;
    }
}

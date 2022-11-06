package com.example.screamlarkbot.configs;

import com.example.screamlarkbot.utils.Emote;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
@EnableRetry
public class BotConfiguration {

    @Value("${screamlark-bot.channel-name}")
    private String channelName;
    @Value("${screamlark-bot.access-token}")
    private String accessToken;
    @Value("${screamlark-bot.client-id}")
    private String clientId;
    @Value("${screamlark-bot.client-secret}")
    private String clientSecret;

    @Bean
    public TwitchClient twitchClient() {
        LocaleContextHolder.setDefaultLocale(Locale.forLanguageTag("ru"));
        OAuth2Credential credential = new OAuth2Credential("twitch", accessToken);
        var client = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withEnableChat(true)
                .withEnablePubSub(true)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withChatAccount(credential)
                .withDefaultAuthToken(credential)
                .build();

        client.getClientHelper().enableFollowEventListener(channelName);
        client.getClientHelper().enableStreamEventListener(channelName);

        var userList = client.getHelix().getUsers(accessToken, null, List.of(channelName)).execute();
        var channelId = userList.getUsers().get(0).getId();
        client.getPubSub().listenForPollEvents(null, channelId);

        client.getChat().joinChannel(channelName);
        client.getChat().sendMessage(channelName, Emote.FROG_WAVE.toString());
        return client;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(getClientHttpRequestFactory());
    }

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        // Connect timeout
        clientHttpRequestFactory.setConnectTimeout(10_000);
        // Read timeout
        clientHttpRequestFactory.setReadTimeout(10_000);
        return clientHttpRequestFactory;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Bot-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
